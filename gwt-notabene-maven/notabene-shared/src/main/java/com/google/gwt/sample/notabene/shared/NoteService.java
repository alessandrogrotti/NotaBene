package com.google.gwt.sample.notabene.shared;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import java.util.List;

//Gestione operaz sulle note
@RemoteServiceRelativePath("noteService")
public interface NoteService extends RemoteService {
    

    boolean createNote(Note note) throws IllegalArgumentException;
    
    List<Note> getUserNotes(String username);
    
    List<Note> getAccessibleNotes(String username);
 
    Note getNoteById(String noteId, String username);
 
    boolean updateNote(Note note, String username);

    boolean deleteNote(String noteId, String username);
    
    Note duplicateNote(String noteId, String username) throws IllegalArgumentException;
}
