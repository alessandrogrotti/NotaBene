package com.google.gwt.sample.notabene.shared;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface NoteLockServiceAsync {
    
    void acquireLock(String noteId, String username, AsyncCallback<NoteLock> callback);
    
    void releaseLock(String noteId, String username, AsyncCallback<Boolean> callback);
    
    void checkLock(String noteId, AsyncCallback<NoteLock> callback);
    
    void renewLock(String noteId, String username, AsyncCallback<Boolean> callback);
    
    void forceReleaseLock(String noteId, String requestingUser, AsyncCallback<Boolean> callback);
}
