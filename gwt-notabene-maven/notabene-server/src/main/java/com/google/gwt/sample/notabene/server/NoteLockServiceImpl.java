package com.google.gwt.sample.notabene.server;

import com.google.gwt.sample.notabene.shared.NoteLock;
import com.google.gwt.sample.notabene.shared.NoteLockService;
import com.google.gwt.sample.notabene.shared.Note;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

// Implementazione del servizio di lock delle note
public class NoteLockServiceImpl extends RemoteServiceServlet implements NoteLockService {
    
    private static final long serialVersionUID = 1L;
    private final NoteLockManager lockManager;
    private final NoteRepository noteRepository;
    
    public NoteLockServiceImpl() {
        this.lockManager = NoteLockManager.getInstance();
        this.noteRepository = NoteRepository.getInstance();
        System.out.println("NoteLockServiceImpl inizializzato");
    }

    // Acquisizione del lock su una nota
    @Override
    public NoteLock acquireLock(String noteId, String username) throws IllegalArgumentException {
        
        if (noteId == null || noteId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID della nota è obbligatorio");
        }
        
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username è obbligatorio");
        }
        
        try {
            
            Note note = noteRepository.getNote(noteId);
            if (note == null) {
                throw new IllegalArgumentException("Nota non trovata: " + noteId);
            }
            
            
            if (!note.canWrite(username)) {
                throw new IllegalArgumentException("Non hai i permessi per modificare questa nota");
            }
            
            
            NoteLock lock = lockManager.acquireLock(noteId, username);
            
            if (lock == null) {
                
                NoteLock existingLock = lockManager.checkLock(noteId);
                if (existingLock != null) {
                    throw new IllegalArgumentException("Nota attualmente in modifica da: " + 
                                                    existingLock.getLockedByUser() + 
                                                    ". Tempo rimanente: " + 
                                                    (existingLock.getRemainingTime() / 1000) + " secondi");
                } else {
                    throw new IllegalArgumentException("Impossibile acquisire il lock sulla nota");
                }
            }
            
            System.out.println("Lock acquisito con successo per utente " + username + " su nota " + noteId);
            return lock;
            
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("Errore durante l'acquisizione del lock: " + e.getMessage());
            e.printStackTrace();
            throw new IllegalArgumentException("Errore durante l'acquisizione del lock: " + e.getMessage());
        }
    }

    // Rilascio del lock su una nota
    @Override
    public boolean releaseLock(String noteId, String username) throws IllegalArgumentException {
        
        if (noteId == null || noteId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID della nota è obbligatorio");
        }
        
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username è obbligatorio");
        }
        
        try {
            boolean result = lockManager.releaseLock(noteId, username);
            
            if (result) {
                System.out.println("Lock rilasciato con successo per utente " + username + " su nota " + noteId);
            } else {
                System.out.println("Impossibile rilasciare lock per utente " + username + " su nota " + noteId + 
                                " (lock non esistente o non di proprietà)");
            }
            
            return result;
            
        } catch (Exception e) {
            System.err.println("Errore durante il rilascio del lock: " + e.getMessage());
            e.printStackTrace();
            throw new IllegalArgumentException("Errore durante il rilascio del lock: " + e.getMessage());
        }
    }
    
    @Override
    public NoteLock checkLock(String noteId) {
        if (noteId == null || noteId.trim().isEmpty()) {
            return null;
        }
        
        try {
            return lockManager.checkLock(noteId);
        } catch (Exception e) {
            System.err.println("Errore durante il controllo del lock: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    public boolean renewLock(String noteId, String username) throws IllegalArgumentException {
        
        if (noteId == null || noteId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID della nota è obbligatorio");
        }
        
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username è obbligatorio");
        }
        
        try {
            boolean result = lockManager.renewLock(noteId, username);
            
            if (result) {
                System.out.println("Lock rinnovato con successo per utente " + username + " su nota " + noteId);
            } else {
                System.out.println("Impossibile rinnovare lock per utente " + username + " su nota " + noteId);
            }
            
            return result;
            
        } catch (Exception e) {
            System.err.println("Errore durante il rinnovo del lock: " + e.getMessage());
            e.printStackTrace();
            throw new IllegalArgumentException("Errore durante il rinnovo del lock: " + e.getMessage());
        }
    }
    
    @Override
    public boolean forceReleaseLock(String noteId, String requestingUser) throws IllegalArgumentException {
        
        if (noteId == null || noteId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID della nota è obbligatorio");
        }
        
        if (requestingUser == null || requestingUser.trim().isEmpty()) {
            throw new IllegalArgumentException("Username è obbligatorio");
        }
        
        try {
            
            Note note = noteRepository.getNote(noteId);
            if (note == null) {
                throw new IllegalArgumentException("Nota non trovata: " + noteId);
            }
            
            
            boolean isOwner = note.getOwnerUsername().equals(requestingUser);
            
            if (!isOwner) {
                throw new IllegalArgumentException("Solo il proprietario della nota può forzare il rilascio del lock");
            }
            
            boolean result = lockManager.forceReleaseLock(noteId, requestingUser, isOwner);
            
            if (result) {
                System.out.println("Lock forzatamente rilasciato da " + requestingUser + " su nota " + noteId);
            } else {
                System.out.println("Impossibile forzare il rilascio del lock per nota " + noteId);
            }
            
            return result;
            
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("Errore durante il force release del lock: " + e.getMessage());
            e.printStackTrace();
            throw new IllegalArgumentException("Errore durante il force release del lock: " + e.getMessage());
        }
    }
}
