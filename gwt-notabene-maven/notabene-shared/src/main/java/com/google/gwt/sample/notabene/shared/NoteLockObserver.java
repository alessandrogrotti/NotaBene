package com.google.gwt.sample.notabene.shared;

// Observer per i lock delle note
public interface NoteLockObserver {
    
    void onNoteLocked(String noteId, String lockedByUser);

    void onNoteUnlocked(String noteId, String unlockedByUser);
    
    void onNoteLockConflict(String noteId, String lockedByUser, String requestingUser);
}
