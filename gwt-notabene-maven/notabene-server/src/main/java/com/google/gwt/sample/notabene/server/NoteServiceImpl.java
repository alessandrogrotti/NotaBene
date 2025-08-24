package com.google.gwt.sample.notabene.server;

import java.util.List;
import java.util.LinkedList;
import java.util.HashSet;
import com.google.gwt.sample.notabene.shared.Note;
import com.google.gwt.sample.notabene.shared.NoteService;
import com.google.gwt.sample.notabene.shared.NoteVersion;
import com.google.gwt.sample.notabene.shared.NotePermission;
import com.google.gwt.sample.notabene.shared.NoteLock;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class NoteServiceImpl extends RemoteServiceServlet implements NoteService {
    
    private static final long serialVersionUID = 1L;
    private NoteRepository noteRepository;
    private NoteLockManager lockManager;
    
    public NoteServiceImpl() {
        noteRepository = NoteRepository.getInstance();
        lockManager = NoteLockManager.getInstance();
        System.out.println("NoteServiceImpl inizializzato con gestione lock");
    }
    
    @Override
    public boolean createNote(Note note) throws IllegalArgumentException {
        
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
                noteRepository.printAllNotes();
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
            
            
            NoteLock lock = lockManager.checkLock(note.getId());
            if (lock != null && !lock.isOwnedBy(username)) {
                System.out.println("Tentativo di aggiornamento nota " + note.getId() + 
                                 " da parte di " + username + " - bloccata da " + lock.getLockedByUser());
                return false;
            }

            if (!existingNote.getContent().equals(note.getContent()) || 
                !existingNote.getTitle().equals(note.getTitle()) ||
                !existingNote.getTags().equals(note.getTags())) {

                NoteVersion version = existingNote.createVersion();
                existingNote.addVersion(version);
                existingNote.incrementVersionNumber();

                System.out.println("Creata versione " + version.getVersionNumber() + 
                                " per la nota " + existingNote.getId() + 
                                " da parte dell'utente " + username);

                existingNote.setTitle(note.getTitle());
                existingNote.setContent(note.getContent());
                existingNote.setTags(note.getTags());
                existingNote.setPermission(note.getPermission());
                existingNote.setReadOnlyUsers(note.getReadOnlyUsers());
                existingNote.setWriteUsers(note.getWriteUsers());
                existingNote.markAsModified(username);

                note.setVersionNumber(existingNote.getVersionNumber());
                note.setLastModified(existingNote.getLastModified());
                if (existingNote.getVersions() instanceof LinkedList) {
                    note.setVersions((LinkedList<NoteVersion>) existingNote.getVersions());
                } else {
                    note.setVersions(new LinkedList<>(existingNote.getVersions()));
                }
                
                boolean result = noteRepository.saveNote(existingNote);
                
                
                if (result) {
                    lockManager.releaseLock(note.getId(), username);
                    System.out.println("Lock rilasciato automaticamente dopo aggiornamento nota " + note.getId());
                }
                
                return result;
            } else {
                existingNote.setPermission(note.getPermission());
                existingNote.setReadOnlyUsers(note.getReadOnlyUsers());
                existingNote.setWriteUsers(note.getWriteUsers());
                
                boolean result = noteRepository.saveNote(existingNote);
                
                if (result) {
                    lockManager.releaseLock(note.getId(), username);
                    System.out.println("Lock rilasciato automaticamente dopo aggiornamento permessi nota " + note.getId());
                }
                
                return result;
            }
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
    
    @Override
    public Note duplicateNote(String noteId, String username) throws IllegalArgumentException {
        if (noteId == null || noteId.trim().isEmpty() || 
            username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("ID della nota e username sono obbligatori");
        }
        
        try {
            Note originalNote = noteRepository.getNote(noteId);
            
            if (originalNote == null) {
                throw new IllegalArgumentException("Nota non trovata");
            }
            
            if (!originalNote.canRead(username)) {
                throw new IllegalArgumentException("Non hai i permessi per leggere questa nota");
            }
            
            Note duplicatedNote = new Note();
            duplicatedNote.setTitle(originalNote.getTitle());
            duplicatedNote.setContent(originalNote.getContent());
            duplicatedNote.setOwnerUsername(username); 
            duplicatedNote.setTags(new HashSet<>(originalNote.getTags())); 
            duplicatedNote.setPermission(NotePermission.PRIVATE); 
            
            
            duplicatedNote.setId(username + "_" + System.currentTimeMillis() + Math.random());
            duplicatedNote.setVersionNumber(0);
            
            boolean result = noteRepository.saveNote(duplicatedNote);
            
            if (result) {
                System.out.println("Nota duplicata con successo. ID originale: " + noteId + 
                                 ", ID copia: " + duplicatedNote.getId() + 
                                 ", Utente: " + username);
                noteRepository.printAllNotes(); 
                return duplicatedNote;
            } else {
                throw new IllegalArgumentException("Errore durante il salvataggio della nota duplicata");
            }
            
        } catch (IllegalArgumentException e) {
            throw e; 
        } catch (Exception e) {
            System.err.println("Errore durante la duplicazione della nota: " + e.getMessage());
            e.printStackTrace();
            throw new IllegalArgumentException("Errore durante la duplicazione della nota: " + e.getMessage());
        }
    }
    
    @Override
    public boolean removeUserFromNote(String noteId, String usernameToRemove, String requestingUsername) throws IllegalArgumentException {
        if (noteId == null || noteId.trim().isEmpty()) {
            throw new IllegalArgumentException("L'ID della nota è obbligatorio");
        }
        
        if (usernameToRemove == null || usernameToRemove.trim().isEmpty()) {
            throw new IllegalArgumentException("L'username da rimuovere è obbligatorio");
        }
        
        if (requestingUsername == null || requestingUsername.trim().isEmpty()) {
            throw new IllegalArgumentException("L'username richiedente è obbligatorio");
        }
        
        try {
            Note note = noteRepository.getNote(noteId);
            if (note == null) {
                throw new IllegalArgumentException("Nota non trovata");
            }
            
            if (!note.getOwnerUsername().equals(requestingUsername) && !usernameToRemove.equals(requestingUsername)) {
                throw new IllegalArgumentException("Non hai i permessi per rimuovere questo utente dalla nota");
            }
            
            if (note.getOwnerUsername().equals(usernameToRemove)) {
                throw new IllegalArgumentException("Il proprietario della nota non può rimuovere se stesso");
            }
            
            boolean removed = false;
            if (note.getReadOnlyUsers().contains(usernameToRemove)) {
                note.getReadOnlyUsers().remove(usernameToRemove);
                removed = true;
            }
            if (note.getWriteUsers().contains(usernameToRemove)) {
                note.getWriteUsers().remove(usernameToRemove);
                removed = true;
            }
            
            if (!removed) {
                throw new IllegalArgumentException("L'utente non ha permessi su questa nota");
            }
            
            boolean result = noteRepository.saveNote(note);
            
            if (result) {
                System.out.println("Utente " + usernameToRemove + " rimosso dalla nota " + noteId + " da " + requestingUsername);
            }
            
            return result;
            
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("Errore durante la rimozione dell'utente dalla nota: " + e.getMessage());
            e.printStackTrace();
            throw new IllegalArgumentException("Errore durante la rimozione dell'utente dalla nota: " + e.getMessage());
        }
    }
   
}
