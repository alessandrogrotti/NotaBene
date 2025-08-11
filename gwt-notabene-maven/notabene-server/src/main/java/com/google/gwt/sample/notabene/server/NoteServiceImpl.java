package com.google.gwt.sample.notabene.server;

import java.util.List;
import java.util.LinkedList;
import com.google.gwt.sample.notabene.shared.Note;
import com.google.gwt.sample.notabene.shared.NoteService;
import com.google.gwt.sample.notabene.shared.NoteVersion;
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
            
            note.setVersionNumber(0);
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

     @Override
    public List<Note> getUserNotes(String username) {
        if (username == null || username.trim().isEmpty()) {
            return List.of(); 
        }
        
        try {
            return noteRepository.getUserNotes(username);
        } catch (Exception e) {
            System.err.println("Errore durante il recupero delle note dell'utente: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }
    
    @Override
    public List<Note> getAccessibleNotes(String username) {
        if (username == null || username.trim().isEmpty()) {
            return List.of(); 
        }
        
        try {
            return noteRepository.getAccessibleNotes(username);
        } catch (Exception e) {
            System.err.println("Errore durante il recupero delle note accessibili all'utente: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }
    
    @Override
    public Note getNoteById(String noteId, String username) {
        if (noteId == null || noteId.trim().isEmpty() || 
            username == null || username.trim().isEmpty()) {
            return null;
        }
        
        try {
            Note note = noteRepository.getNote(noteId);
            
            if (note != null && note.canRead(username)) {
                return note;
            }
            
            return null;
        } catch (Exception e) {
            System.err.println("Errore durante il recupero della nota: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    public boolean updateNote(Note note, String username) {
        if (note == null || username == null || username.trim().isEmpty()) {
            return false;
        }
        
        try {
            Note existingNote = noteRepository.getNote(note.getId());
            
            if (existingNote == null || !existingNote.canWrite(username)) {
                return false;
            }

            if (!existingNote.getContent().equals(note.getContent()) || 
                !existingNote.getTitle().equals(note.getTitle()) ||
                !existingNote.getTags().equals(note.getTags())) {

                NoteVersion version = existingNote.createVersion(username);
                existingNote.addVersion(version);

                existingNote.incrementVersionNumber();

                System.out.println("Creata versione " + version.getVersionNumber() + 
                                " per la nota " + existingNote.getId() + 
                                " da parte dell'utente " + username);

                note.markAsModified(username);

                note.setVersionNumber(existingNote.getVersionNumber());
                if (existingNote.getVersions() instanceof LinkedList) {
                    note.setVersions((LinkedList<NoteVersion>) existingNote.getVersions());
                } else {
                    note.setVersions(new LinkedList<>(existingNote.getVersions()));
                }
            } else {
                note.markAsModified(username);
                note.setVersionNumber(existingNote.getVersionNumber());
                if (existingNote.getVersions() instanceof LinkedList) {
                    note.setVersions((LinkedList<NoteVersion>) existingNote.getVersions());
                } else {
                    note.setVersions(new LinkedList<>(existingNote.getVersions()));
                }
            }

            return noteRepository.saveNote(note);
        } catch (Exception e) {
            System.err.println("Errore durante l'aggiornamento della nota: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean deleteNote(String noteId, String username) {
        if (noteId == null || noteId.trim().isEmpty() || 
            username == null || username.trim().isEmpty()) {
            return false;
        }
        
        try {
            Note note = noteRepository.getNote(noteId);
            
            if (note == null || !note.getOwnerUsername().equals(username)) {
                return false;
            }
            
            return noteRepository.deleteNote(noteId);
        } catch (Exception e) {
            System.err.println("Errore durante l'eliminazione della nota: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
   
}
