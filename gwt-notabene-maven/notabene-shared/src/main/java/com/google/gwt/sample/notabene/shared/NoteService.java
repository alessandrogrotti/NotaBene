package com.google.gwt.sample.notabene.shared;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import java.util.List;

//Gestione operaz sulle note
@RemoteServiceRelativePath("noteService")
public interface NoteService extends RemoteService {
    

    boolean createNote(Note note) throws IllegalArgumentException;
    
}
