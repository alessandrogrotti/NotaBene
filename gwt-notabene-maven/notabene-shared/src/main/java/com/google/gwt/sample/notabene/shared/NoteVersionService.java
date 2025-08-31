package com.google.gwt.sample.notabene.shared;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import java.util.List;

//Servizio per la gestione delle versioni delle note
@RemoteServiceRelativePath("noteVersionService")
public interface NoteVersionService extends RemoteService {
    
    List<NoteVersion> getNoteVersions(String noteId);
    
    NoteVersion getNoteVersion(String versionId);
    
    boolean hasVersionHistory(String noteId);
    
    int getVersionCount(String noteId);
    
    boolean saveNoteVersion(NoteVersion version);
    
    boolean deleteAllVersionsForNote(String noteId);
}