package com.google.gwt.sample.notabene.shared;

import com.google.gwt.user.client.rpc.AsyncCallback;
import java.util.List;

//Interfaccia asincrona per il servizio NoteService
public interface NoteServiceAsync {
    
    void createNote(Note note, AsyncCallback<Boolean> callback);
    
    void getUserNotes(String username, AsyncCallback<List<Note>> callback);
 
    void getAccessibleNotes(String username, AsyncCallback<List<Note>> callback);

    void getNoteById(String noteId, String username, AsyncCallback<Note> callback);
 
    void updateNote(Note note, String username, AsyncCallback<Boolean> callback);
    
    void deleteNote(String noteId, String username, AsyncCallback<Boolean> callback);
}
