package com.google.gwt.sample.notabene.shared;

import java.io.Serializable;
import com.google.gwt.user.client.rpc.IsSerializable;

public class NoteLock implements Serializable, IsSerializable {
    
    private static final long serialVersionUID = 1L;
    
    private String noteId;
    private String lockedByUser;
    private long lockTimestamp;
    private long lockDuration; 
    
    public NoteLock() {
    }
    
    public NoteLock(String noteId, String lockedByUser) {
        this.noteId = noteId;
        this.lockedByUser = lockedByUser;
        this.lockTimestamp = System.currentTimeMillis();
        this.lockDuration = 20 * 60 * 1000; 
    }
    
    public NoteLock(String noteId, String lockedByUser, long lockDurationMs) {
        this.noteId = noteId;
        this.lockedByUser = lockedByUser;
        this.lockTimestamp = System.currentTimeMillis();
        this.lockDuration = lockDurationMs;
    }
    
    
    public boolean isValid() {
        return (System.currentTimeMillis() - lockTimestamp) < lockDuration;
    }
    
    public boolean isOwnedBy(String username) {
        return lockedByUser != null && lockedByUser.equals(username);
    }
    
    public long getRemainingTime() {
        long elapsed = System.currentTimeMillis() - lockTimestamp;
        return Math.max(0, lockDuration - elapsed);
    }
    
    public void extendLock(long additionalTime) {
        this.lockDuration += additionalTime;
    }
    
    public void renewLock() {
        this.lockTimestamp = System.currentTimeMillis();
    }
    
    public String getNoteId() {
        return noteId;
    }
    
    public void setNoteId(String noteId) {
        this.noteId = noteId;
    }
    
    public String getLockedByUser() {
        return lockedByUser;
    }
    
    public void setLockedByUser(String lockedByUser) {
        this.lockedByUser = lockedByUser;
    }
    
    public long getLockTimestamp() {
        return lockTimestamp;
    }
    
    public void setLockTimestamp(long lockTimestamp) {
        this.lockTimestamp = lockTimestamp;
    }
    
    public long getLockDuration() {
        return lockDuration;
    }
    
    public void setLockDuration(long lockDuration) {
        this.lockDuration = lockDuration;
    }
    
    @Override
    public String toString() {
        return "NoteLock{" +
                "noteId='" + noteId + '\'' +
                ", lockedByUser='" + lockedByUser + '\'' +
                ", lockTimestamp=" + lockTimestamp +
                ", lockDuration=" + lockDuration +
                ", isValid=" + isValid() +
                '}';
    }
}
