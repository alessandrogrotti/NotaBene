package com.google.gwt.sample.notabene.shared;

import com.google.gwt.user.client.rpc.AsyncCallback;
import java.util.List;

public interface NoteVersionServiceAsync {
    
    void getNoteVersions(String noteId, AsyncCallback<List<NoteVersion>> callback);
    
    void getNoteVersion(String versionId, AsyncCallback<NoteVersion> callback);
    
    void hasVersionHistory(String noteId, AsyncCallback<Boolean> callback);
    
    void getVersionCount(String noteId, AsyncCallback<Integer> callback);
    
    void saveNoteVersion(NoteVersion version, AsyncCallback<Boolean> callback);
    
    void deleteAllVersionsForNote(String noteId, AsyncCallback<Boolean> callback);
}