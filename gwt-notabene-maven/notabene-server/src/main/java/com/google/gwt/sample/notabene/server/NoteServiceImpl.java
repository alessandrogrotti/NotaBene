package com.google.gwt.sample.notabene.server;

import com.google.gwt.sample.notabene.shared.Note;
import com.google.gwt.sample.notabene.shared.NoteService;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class NoteServiceImpl extends RemoteServiceServlet implements NoteService {
    
    private static final long serialVersionUID = 1L;
    private NoteRepository noteRepository;
    
    public NoteServiceImpl() {
        noteRepository = NoteRepository.getInstance();
        System.out.println("NoteServiceImpl inizializzato");
    }
    
    @Override
    public boolean createNote(Note note) throws IllegalArgumentException {
        // Validazione dei dati di input
        if (note == null) {
            throw new IllegalArgumentException("La nota non può essere null");
        }
        
        if (note.getTitle() == null || note.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Il titolo della nota è obbligatorio");
        }
        
        if (note.getContent() == null || note.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Il contenuto della nota è obbligatorio");
        }
        
        if (note.getOwnerUsername() == null || note.getOwnerUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("L'owner della nota è obbligatorio");
        }
        
        if (note.getTitle().length() > 50) {
            throw new IllegalArgumentException("Il titolo non può superare i 50 caratteri");
        }
        
        if (note.getContent().length() > 280) {
            throw new IllegalArgumentException("Il contenuto non può superare i 280 caratteri");
        }
        
        try {
            if (note.getId() == null) {
                note.setId(note.getOwnerUsername() + "_" + System.currentTimeMillis());
            }
            
            if (noteRepository.noteExists(note.getId())) {
                throw new IllegalArgumentException("Esiste già una nota con questo ID");
            }
            
        
            boolean result = noteRepository.saveNote(note);
            
            if (result) {
                System.out.println("Nota creata con successo per l'utente: " + note.getOwnerUsername());
                noteRepository.printAllNotes(); // Per debug
            }
            
            return result;
        } catch (Exception e) {
            System.err.println("Errore durante la creazione della nota: " + e.getMessage());
            e.printStackTrace();
            throw new IllegalArgumentException("Errore durante la creazione della nota: " + e.getMessage());
        }
    }
    
   
}
