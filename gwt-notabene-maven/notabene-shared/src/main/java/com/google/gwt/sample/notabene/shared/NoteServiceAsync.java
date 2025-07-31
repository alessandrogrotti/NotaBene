package com.google.gwt.sample.notabene.shared;

import com.google.gwt.user.client.rpc.AsyncCallback;
import java.util.List;

//Interfaccia asincrona per il servizio NoteService
public interface NoteServiceAsync {
    
    void createNote(Note note, AsyncCallback<Boolean> callback);
    
}
