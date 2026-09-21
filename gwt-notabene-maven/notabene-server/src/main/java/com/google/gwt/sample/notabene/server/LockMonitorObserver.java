package com.google.gwt.sample.notabene.server;

import com.google.gwt.sample.notabene.shared.NoteLockObserver;

// Observer per il monitoraggio dei lock sulle note
public class LockMonitorObserver implements NoteLockObserver {
    
    private String observerName;
    
    public LockMonitorObserver(String name) {
        this.observerName = name;
    }
    
    // Acquisizione
    @Override
    public void onNoteLocked(String noteId, String lockedByUser) {
        System.out.println("[" + observerName + "] LOCK ACQUISITO: Nota '" + noteId + 
                          "' bloccata da utente '" + lockedByUser + "'");
    }
    
    // Rilascio
    @Override
    public void onNoteUnlocked(String noteId, String unlockedByUser) {
        System.out.println("[" + observerName + "] LOCK RILASCIATO: Nota '" + noteId + 
                          "' sbloccata da utente '" + unlockedByUser + "'");
    }
    
    // Gestione conflitto
    @Override
    public void onNoteLockConflict(String noteId, String lockedByUser, String requestingUser) {
        System.out.println("[" + observerName + "] CONFLITTO LOCK: Utente '" + requestingUser + 
                          "' ha tentato di modificare la nota '" + noteId + 
                          "' già bloccata da '" + lockedByUser + "'");
    }
    
    public String getObserverName() {
        return observerName;
    }
}
