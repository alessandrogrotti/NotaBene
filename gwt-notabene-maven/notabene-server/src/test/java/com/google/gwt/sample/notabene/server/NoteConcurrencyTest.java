package com.google.gwt.sample.notabene.server;

import com.google.gwt.sample.notabene.shared.NoteLock;
import com.google.gwt.sample.notabene.shared.NoteLockObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class NoteConcurrencyTest {
    private NoteLockManager lockManager;
    
    @BeforeEach
    void setUp() {
        lockManager = NoteLockManager.getInstance();
        lockManager.clearAllLocks();
    }

    @Test
    void testSingletonPattern() {
        NoteLockManager instance1 = NoteLockManager.getInstance();
        NoteLockManager instance2 = NoteLockManager.getInstance();
        assertSame(instance1, instance2, "NoteLockManager deve essere un singleton");
    }

    @Test
    void testAcquireLockSuccess() {
        String noteId = "test_note_1";
        String username = "mario";
        
        NoteLock lock = lockManager.acquireLock(noteId, username);
        
        assertNotNull(lock, "Il lock dovrebbe essere acquisito con successo");
        assertEquals(noteId, lock.getNoteId());
        assertEquals(username, lock.getLockedByUser());
        assertTrue(lock.isValid(), "Il lock dovrebbe essere valido");
        assertTrue(lock.isOwnedBy(username), "Il lock dovrebbe appartenere all'utente");
        
        lockManager.releaseLock(noteId, username);
    }

    @Test
    void testAcquireLockAlreadyLocked() {
        String noteId = "test_note_2";
        String user1 = "mario";
        String user2 = "luigi";
        
        NoteLock lock1 = lockManager.acquireLock(noteId, user1);
        assertNotNull(lock1, "Il primo lock dovrebbe essere acquisito");
        
        NoteLock lock2 = lockManager.acquireLock(noteId, user2);
        assertNull(lock2, "Il secondo lock non dovrebbe essere acquisito - concorrenza bloccata");
        
        lockManager.releaseLock(noteId, user1);
    }

    @Test
    void testRenewLockSuccess() {
        String noteId = "test_note_3";
        String username = "mario";
        
        NoteLock originalLock = lockManager.acquireLock(noteId, username);
        assertNotNull(originalLock);
        long originalTimestamp = originalLock.getLockTimestamp();
        
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        boolean renewed = lockManager.renewLock(noteId, username);
        assertTrue(renewed, "Il lock dovrebbe essere rinnovato con successo");
        
        NoteLock renewedLock = lockManager.checkLock(noteId);
        assertNotNull(renewedLock);
        assertTrue(renewedLock.getLockTimestamp() > originalTimestamp, 
                  "Il timestamp dovrebbe essere aggiornato dopo il rinnovo");
        
        lockManager.releaseLock(noteId, username);
    }

    @Test
    void testReleaseLockSuccess() {
        String noteId = "test_note_4";
        String username = "mario";
        
        lockManager.acquireLock(noteId, username);
        boolean released = lockManager.releaseLock(noteId, username);
        assertTrue(released, "Il lock dovrebbe essere rilasciato con successo");
        
        NoteLock lock = lockManager.checkLock(noteId);
        assertNull(lock, "Il lock non dovrebbe più esistere dopo il rilascio");
    }

    @Test
    void testMultipleNotesLocking() {
        String username = "mario";
        String noteId1 = "test_note_5";
        String noteId2 = "test_note_6";
        
        NoteLock lock1 = lockManager.acquireLock(noteId1, username);
        NoteLock lock2 = lockManager.acquireLock(noteId2, username);
        
        assertNotNull(lock1, "Il primo lock dovrebbe essere acquisito");
        assertNotNull(lock2, "Il secondo lock dovrebbe essere acquisito");
        
        lockManager.releaseLock(noteId1, username);
        lockManager.releaseLock(noteId2, username);
    }

    @Test
    void testObserverPattern() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(2);
        AtomicInteger lockAcquiredCount = new AtomicInteger(0);
        AtomicInteger lockReleasedCount = new AtomicInteger(0);
        
        NoteLockObserver observer = new NoteLockObserver() {
            @Override
            public void onNoteLocked(String noteId, String lockedByUser) {
                lockAcquiredCount.incrementAndGet();
                latch.countDown();
            }
            
            @Override
            public void onNoteUnlocked(String noteId, String unlockedByUser) {
                lockReleasedCount.incrementAndGet();
                latch.countDown();
            }
            
            @Override
            public void onNoteLockConflict(String noteId, String lockedByUser, String requestingUser) {
                
            }
        };
        
        lockManager.addObserver(observer);
        
        String noteId = "test_note_observer";
        String username = "mario";
        
        lockManager.acquireLock(noteId, username);
        lockManager.releaseLock(noteId, username);
        
        assertTrue(latch.await(1, TimeUnit.SECONDS), 
                  "Le notifiche degli observer dovrebbero arrivare entro 1 secondo");
        
        assertEquals(1, lockAcquiredCount.get(), "Dovrebbe esserci 1 notifica di acquisizione");
        assertEquals(1, lockReleasedCount.get(), "Dovrebbe esserci 1 notifica di rilascio");
        
        lockManager.removeObserver(observer);
    }

    @Test
    void testConflictNotification() throws InterruptedException {
        CountDownLatch conflictLatch = new CountDownLatch(1);
        AtomicBoolean conflictDetected = new AtomicBoolean(false);
        
        NoteLockObserver observer = new NoteLockObserver() {
            @Override
            public void onNoteLocked(String noteId, String lockedByUser) {}
            
            @Override
            public void onNoteUnlocked(String noteId, String unlockedByUser) {}
            
            @Override
            public void onNoteLockConflict(String noteId, String lockedByUser, String requestingUser) {
                conflictDetected.set(true);
                conflictLatch.countDown();
            }
        };
        
        lockManager.addObserver(observer);
        
        String noteId = "test_note_conflict";
        String user1 = "mario";
        String user2 = "luigi";
        
        lockManager.acquireLock(noteId, user1);
        lockManager.acquireLock(noteId, user2);
        
        assertTrue(conflictLatch.await(1, TimeUnit.SECONDS), 
                  "Dovrebbe essere rilevato un conflitto entro 1 secondo");
        assertTrue(conflictDetected.get(), "Dovrebbe essere stato rilevato un conflitto");
        
        lockManager.removeObserver(observer);
        lockManager.releaseLock(noteId, user1);
    }

    @Test
    @Timeout(10)
    void testConcurrentLockAcquisition() throws InterruptedException {
        String noteId = "test_note_concurrent";
        int threadCount = 10;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        
        for (int i = 0; i < threadCount; i++) {
            final String username = "user" + i;
            new Thread(() -> {
                try {
                    startLatch.await();
                    NoteLock lock = lockManager.acquireLock(noteId, username);
                    if (lock != null) {
                        successCount.incrementAndGet();
                        Thread.sleep(50);
                        lockManager.releaseLock(noteId, username);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown();
                }
            }).start();
        }
        
        startLatch.countDown();
        endLatch.await();
        
        assertEquals(1, successCount.get(), 
                    "Solo un thread dovrebbe essere riuscito ad acquisire il lock");
    }

    @Test
    @Timeout(30)
    void testMassiveConcurrentLockAcquisition() throws InterruptedException {
        String noteId = "stress_test_note";
        int threadCount = 100;
        ExecutorService executor = Executors.newFixedThreadPool(50);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completionLatch = new CountDownLatch(threadCount);
        
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);
        
        for (int i = 0; i < threadCount; i++) {
            final String username = "user_" + i;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    
                    NoteLock lock = lockManager.acquireLock(noteId, username);
                    if (lock != null) {
                        successCount.incrementAndGet();
                        Thread.sleep(10);
                        lockManager.releaseLock(noteId, username);
                    } else {
                        conflictCount.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    completionLatch.countDown();
                }
            });
        }
        
        startLatch.countDown();
        assertTrue(completionLatch.await(25, TimeUnit.SECONDS), 
                  "Tutti i thread dovrebbero completare entro 25 secondi");
        
        executor.shutdown();
        
        assertTrue(successCount.get() > 0, "Almeno un thread dovrebbe aver acquisito il lock");
        assertTrue(conflictCount.get() > 0, "Dovrebbero esserci dei conflitti");
        assertEquals(threadCount, successCount.get() + conflictCount.get(), 
                    "Tutti i thread dovrebbero essere stati processati");
        assertEquals(0, lockManager.getActiveLocks().size(), 
                    "Non dovrebbero rimanere lock attivi");
        
        System.out.println("Stress Test: " + successCount.get() + " successi, " + 
                         conflictCount.get() + " conflitti");
    }

    @Test
    @Timeout(15)
    void testRaceConditionOnSameNote() throws InterruptedException {
        String noteId = "race_condition_note";
        int attempts = 500;
        ExecutorService executor = Executors.newFixedThreadPool(10);
        
        AtomicInteger user1Wins = new AtomicInteger(0);
        AtomicInteger user2Wins = new AtomicInteger(0);
        AtomicInteger inconsistencies = new AtomicInteger(0);
        
        for (int i = 0; i < attempts; i++) {
            CountDownLatch roundLatch = new CountDownLatch(2);
            AtomicReference<NoteLock> lock1 = new AtomicReference<>();
            AtomicReference<NoteLock> lock2 = new AtomicReference<>();
            
            executor.submit(() -> {
                lock1.set(lockManager.acquireLock(noteId, "user1"));
                roundLatch.countDown();
            });
            
            executor.submit(() -> {
                lock2.set(lockManager.acquireLock(noteId, "user2"));
                roundLatch.countDown();
            });
            
            assertTrue(roundLatch.await(1, TimeUnit.SECONDS), 
                      "Round " + i + " dovrebbe completare rapidamente");
            
            NoteLock l1 = lock1.get();
            NoteLock l2 = lock2.get();
            
            if (l1 != null && l2 != null) {
                inconsistencies.incrementAndGet();
            } else if (l1 != null) {
                user1Wins.incrementAndGet();
                lockManager.releaseLock(noteId, "user1");
            } else if (l2 != null) {
                user2Wins.incrementAndGet();
                lockManager.releaseLock(noteId, "user2");
            }
            
            lockManager.clearAllLocks();
        }
        
        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS), 
                  "Executor dovrebbe terminare");
        
        assertEquals(0, inconsistencies.get(), "Non dovrebbero esserci inconsistenze");
        assertTrue(user1Wins.get() > 0, "User1 dovrebbe vincere almeno una volta");
        assertTrue(user2Wins.get() > 0, "User2 dovrebbe vincere almeno una volta");
        
        System.out.println("Race Condition Test: User1=" + user1Wins.get() + 
                         ", User2=" + user2Wins.get() + ", Inconsistenze=" + inconsistencies.get());
    }

    @Test
    @Timeout(15)
    void testObserverNotificationConsistency() throws InterruptedException {
        String noteId = "observer_test_note";
        AtomicInteger lockNotifications = new AtomicInteger(0);
        AtomicInteger unlockNotifications = new AtomicInteger(0);
        AtomicInteger conflictNotifications = new AtomicInteger(0);
        
        NoteLockObserver observer = new NoteLockObserver() {
            @Override
            public void onNoteLocked(String id, String user) {
                lockNotifications.incrementAndGet();
            }
            
            @Override
            public void onNoteUnlocked(String id, String user) {
                unlockNotifications.incrementAndGet();
            }
            
            @Override
            public void onNoteLockConflict(String id, String lockedBy, String requesting) {
                conflictNotifications.incrementAndGet();
            }
        };
        
        lockManager.addObserver(observer);
        
        ExecutorService executor = Executors.newFixedThreadPool(10);
        int operationCount = 100;
        CountDownLatch completionLatch = new CountDownLatch(operationCount);
        
        for (int i = 0; i < operationCount; i++) {
            final String username = "user_" + (i % 5);
            executor.submit(() -> {
                try {
                    NoteLock lock = lockManager.acquireLock(noteId, username);
                    if (lock != null) {
                        Thread.sleep(1);
                        lockManager.releaseLock(noteId, username);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    completionLatch.countDown();
                }
            });
        }
        
        assertTrue(completionLatch.await(10, TimeUnit.SECONDS), 
                  "Tutte le operazioni dovrebbero completare");
        executor.shutdown();
        
        Thread.sleep(100); 
        
        lockManager.removeObserver(observer);
        
        assertTrue(lockNotifications.get() > 0, "Dovrebbero esserci notifiche di lock");
        assertTrue(unlockNotifications.get() > 0, "Dovrebbero esserci notifiche di unlock");
        assertTrue(conflictNotifications.get() > 0, "Dovrebbero esserci notifiche di conflitto");
        
        assertEquals(lockNotifications.get(), unlockNotifications.get(), 
                    "Ogni lock dovrebbe corrispondere a un unlock");
        
        System.out.println("Observer Test: " + lockNotifications.get() + " locks, " + 
                         unlockNotifications.get() + " unlocks, " + 
                         conflictNotifications.get() + " conflicts");
    }

    
    
    @Test
    void testLockExpiration() throws InterruptedException {
        String noteId = "test_note_expiration";
        String username = "mario";
        
        NoteLock shortLock = new NoteLock(noteId, username, 100); 
        assertTrue(shortLock.isValid(), "Il lock dovrebbe essere valido inizialmente");
        
        Thread.sleep(200);
        assertFalse(shortLock.isValid(), "Un lock scaduto non dovrebbe essere valido");
    }

    @Test
    @Timeout(20)
    void testLockExpirationUnderConcurrency() throws InterruptedException {
        String noteId = "expiration_test_note";
        
        NoteLock shortLock = new NoteLock(noteId, "original_user", 500); 
        lockManager.getActiveLocks().put(noteId, shortLock);
        
        Thread.sleep(600);
        
        ExecutorService executor = Executors.newFixedThreadPool(20);
        AtomicInteger successCount = new AtomicInteger(0);
        CountDownLatch completionLatch = new CountDownLatch(20);
        
        for (int i = 0; i < 20; i++) {
            final String username = "new_user_" + i;
            executor.submit(() -> {
                try {
                    NoteLock lock = lockManager.acquireLock(noteId, username);
                    if (lock != null) {
                        successCount.incrementAndGet();
                        Thread.sleep(10);
                        lockManager.releaseLock(noteId, username);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    completionLatch.countDown();
                }
            });
        }
        
        assertTrue(completionLatch.await(15, TimeUnit.SECONDS), 
                  "Tutti i thread dovrebbero completare");
        executor.shutdown();
        
        assertTrue(successCount.get() > 0, 
                  "Almeno un thread dovrebbe acquisire il lock dopo la scadenza");
        System.out.println("Expiration Test: " + successCount.get() + 
                         " acquisizioni riuscite dopo scadenza");
    }

    @Test
    @Timeout(20)
    void testMemoryLeakageWithManyLocks() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(20);
        AtomicInteger completedOperations = new AtomicInteger(0);
        
        for (int i = 0; i < 500; i++) {
            final String noteId = "note_" + i;
            final String username = "user_" + (i % 10);
            
            executor.submit(() -> {
                try {
                    NoteLock lock = lockManager.acquireLock(noteId, username);
                    if (lock != null) {
                        Thread.sleep(1);
                        lockManager.releaseLock(noteId, username);
                    }
                    completedOperations.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        
        executor.shutdown();
        assertTrue(executor.awaitTermination(15, TimeUnit.SECONDS), 
                  "Tutte le operazioni dovrebbero completare");
        
        assertTrue(completedOperations.get() > 450, 
                  "La maggior parte delle operazioni dovrebbe completare");
        
        Thread.sleep(1000);
        
        int activeLocks = lockManager.getActiveLocks().size();
        assertTrue(activeLocks < 50, 
                  "Non dovrebbero rimanere troppi lock attivi: " + activeLocks);
        
        System.out.println("Memory Test: " + completedOperations.get() + 
                         " operazioni, " + activeLocks + " lock rimasti");
    }

    @Test
    @Timeout(10)
    void testDeadlockPrevention() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        AtomicInteger operations = new AtomicInteger(0);
        
        for (int i = 0; i < 100; i++) {
            final int index = i;
            executor.submit(() -> {
                String noteId1 = "note_" + (index % 10);
                String noteId2 = "note_" + ((index + 1) % 10);
                String username = "user_" + (index % 5);
                
                lockManager.acquireLock(noteId1, username);
                lockManager.checkLock(noteId2);
                lockManager.renewLock(noteId1, username);
                lockManager.releaseLock(noteId1, username);
                lockManager.cleanupExpiredLocks();
                
                operations.incrementAndGet();
            });
        }
        
        executor.shutdown();
        assertTrue(executor.awaitTermination(8, TimeUnit.SECONDS), 
                  "Tutte le operazioni dovrebbero completare senza deadlock");
        
        assertTrue(operations.get() >= 100, "Tutte le operazioni dovrebbero essere completate");
        System.out.println("Deadlock Test: " + operations.get() + 
                         " operazioni completate senza deadlock");
    }

    @Test
    void testCleanupExpiredLocks() {
        String noteId = "test_note_cleanup";
        String username = "mario";
        
        lockManager.acquireLock(noteId, username);
        lockManager.cleanupExpiredLocks();
        
        NoteLock lockAfterCleanup = lockManager.checkLock(noteId);
        assertNotNull(lockAfterCleanup, 
                     "Un lock valido non dovrebbe essere rimosso dalla pulizia");
        
        lockManager.releaseLock(noteId, username);
    }

    @Test
    void testClearAllLocks() {
        String noteId1 = "test_note_clear1";
        String noteId2 = "test_note_clear2";
        String username = "mario";
        
        lockManager.acquireLock(noteId1, username);
        lockManager.acquireLock(noteId2, username);
        
        assertNotNull(lockManager.checkLock(noteId1));
        assertNotNull(lockManager.checkLock(noteId2));
        
        lockManager.clearAllLocks();
        
        assertNull(lockManager.checkLock(noteId1), 
                  "Tutti i lock dovrebbero essere stati rimossi");
        assertNull(lockManager.checkLock(noteId2), 
                  "Tutti i lock dovrebbero essere stati rimossi");
        assertTrue(lockManager.getActiveLocks().isEmpty(), 
                  "La mappa dei lock attivi dovrebbe essere vuota");
    }
}
