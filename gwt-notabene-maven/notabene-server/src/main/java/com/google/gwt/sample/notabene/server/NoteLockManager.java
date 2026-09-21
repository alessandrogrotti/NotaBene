package com.google.gwt.sample.notabene.server;

import com.google.gwt.sample.notabene.shared.NoteLock;
import com.google.gwt.sample.notabene.shared.NoteLockObserver;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

// Gestore dei lock delle note
public class NoteLockManager {

    private static final NoteLockManager INSTANCE = new NoteLockManager();

    // Creazione della mappa dei lock attivi
    private final ConcurrentHashMap<String, NoteLock> activeLocks = new ConcurrentHashMap<>();
    private final CopyOnWriteArrayList<NoteLockObserver> observers = new CopyOnWriteArrayList<>();
    private final ScheduledExecutorService janitor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "NoteLockManager-Janitor");
        t.setDaemon(true);
        return t;
    });

    private NoteLockManager() {
        janitor.scheduleAtFixedRate(this::cleanupExpiredLocks, 1, 1, TimeUnit.SECONDS);
    }

    public static NoteLockManager getInstance() {
        return INSTANCE;
    }

    // Acquisizione
    public NoteLock acquireLock(String noteId, String username) {
        if (noteId == null || username == null) return null;

        cleanupExpiredLocks();

        NoteLock existing = activeLocks.get(noteId);
        if (existing == null) {
            NoteLock newLock = new NoteLock(noteId, username);
            NoteLock prev = activeLocks.putIfAbsent(noteId, newLock);
            if (prev == null) {
                notifyLocked(noteId, username);
                return newLock;
            }
            existing = prev;
        }

        if (existing.isValid()) {
            if (existing.isOwnedBy(username)) {
                return existing;
            }
            notifyConflict(noteId, existing.getLockedByUser(), username);
            return null;
        } else {
            NoteLock newLock = new NoteLock(noteId, username);
            activeLocks.put(noteId, newLock);
            notifyLocked(noteId, username);
            return newLock;
        }
    }

    // Rilascio
    public boolean releaseLock(String noteId, String username) {
        if (noteId == null || username == null) return false;

        NoteLock existing = activeLocks.get(noteId);
        if (existing != null && existing.isOwnedBy(username)) {
            boolean removed = activeLocks.remove(noteId, existing);
            if (removed) {
                notifyUnlocked(noteId, username);
            }
            return removed;
        }
        return false;
    }

    // Controllo
    public NoteLock checkLock(String noteId) {
        if (noteId == null) return null;
        NoteLock lock = activeLocks.get(noteId);
        if (lock == null) return null;
        if (!lock.isValid()) {
            activeLocks.remove(noteId, lock);
            notifyUnlocked(noteId, lock.getLockedByUser());
            return null;
        }
        return lock;
    }

    // Rinnovo
    public boolean renewLock(String noteId, String username) {
        if (noteId == null || username == null) return false;
        NoteLock lock = activeLocks.get(noteId);
        if (lock != null && lock.isValid() && lock.isOwnedBy(username)) {
            lock.renewLock();
            return true;
        }
        return false;
    }

    // Forzare la release 
    public boolean forceReleaseLock(String noteId, String requestingUser, boolean isOwner) {
        if (noteId == null || requestingUser == null) return false;
        NoteLock lock = activeLocks.get(noteId);
        if (lock == null) return false;

        if (isOwner || lock.isOwnedBy(requestingUser)) {
            boolean removed = activeLocks.remove(noteId, lock);
            if (removed) {
                notifyUnlocked(noteId, lock.getLockedByUser());
            }
            return removed;
        }
        return false;
    }

    public void addObserver(NoteLockObserver o) {
        if (o != null) observers.addIfAbsent(o);
    }

    public void removeObserver(NoteLockObserver o) {
        if (o != null) observers.remove(o);
    }

    public Map<String, NoteLock> getActiveLocks() {
        return activeLocks;
    }

    public void clearAllLocks() {
        activeLocks.clear();
    }

    public void cleanupExpiredLocks() {
        for (Map.Entry<String, NoteLock> e : activeLocks.entrySet()) {
            NoteLock lock = e.getValue();
            if (lock == null) continue;
            if (!lock.isValid()) {
                if (activeLocks.remove(e.getKey(), lock)) {
                    notifyUnlocked(e.getKey(), lock.getLockedByUser());
                }
            }
        }
    }

    private void notifyLocked(String noteId, String user) {
        for (NoteLockObserver o : observers) {
            try { o.onNoteLocked(noteId, user); } catch (Exception ignored) {}
        }
    }

    private void notifyUnlocked(String noteId, String user) {
        for (NoteLockObserver o : observers) {
            try { o.onNoteUnlocked(noteId, user); } catch (Exception ignored) {}
        }
    }

    private void notifyConflict(String noteId, String lockedBy, String requestingUser) {
        for (NoteLockObserver o : observers) {
            try { o.onNoteLockConflict(noteId, lockedBy, requestingUser); } catch (Exception ignored) {}
        }
    }
}
