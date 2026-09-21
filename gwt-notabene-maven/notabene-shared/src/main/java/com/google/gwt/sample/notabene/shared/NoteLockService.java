package com.google.gwt.sample.notabene.shared;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("noteLockService")
public interface NoteLockService extends RemoteService {
    
    NoteLock acquireLock(String noteId, String username) throws IllegalArgumentException;
    
    boolean releaseLock(String noteId, String username) throws IllegalArgumentException;
    
    NoteLock checkLock(String noteId);
    
    boolean renewLock(String noteId, String username) throws IllegalArgumentException;
    
    boolean forceReleaseLock(String noteId, String requestingUser) throws IllegalArgumentException;
}
