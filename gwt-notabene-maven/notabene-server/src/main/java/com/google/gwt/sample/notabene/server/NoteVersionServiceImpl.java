package com.google.gwt.sample.notabene.server;

import com.google.gwt.sample.notabene.shared.Note;
import com.google.gwt.sample.notabene.shared.NoteVersion;
import com.google.gwt.sample.notabene.shared.NoteVersionService;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import java.util.List;
import java.util.ArrayList;

// Implementazione del servizio di versionamento delle note
public class NoteVersionServiceImpl extends RemoteServiceServlet implements NoteVersionService {
    
    private final NoteRepository noteRepository = NoteRepository.getInstance();
    
    @Override
    public List<NoteVersion> getNoteVersions(String noteId) {
        try {
            Note note = noteRepository.getNote(noteId);
            if (note == null) {
                return new ArrayList<>();
            }
            
            return note.getVersions();
        } catch (Exception e) {
            System.err.println("Errore nel recuperare le versioni della nota: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    @Override
    public NoteVersion getNoteVersion(String versionId) {
        try {
            String noteId = extractNoteIdFromVersionId(versionId);
            if (noteId == null) {
                return null;
            }
            
            Note note = noteRepository.getNote(noteId);
            if (note == null) {
                return null;
            }
            
            return note.getVersions().stream()
                    .filter(version -> versionId.equals(version.getVersionId()))
                    .findFirst()
                    .orElse(null);
                    
        } catch (Exception e) {
            System.err.println("Errore nel recuperare la versione specifica: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    public boolean hasVersionHistory(String noteId) {
        try {
            Note note = noteRepository.getNote(noteId);
            return note != null && note.hasVersionHistory();
        } catch (Exception e) {
            System.err.println("Errore nel controllare la cronologia versioni: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public int getVersionCount(String noteId) {
        try {
            Note note = noteRepository.getNote(noteId);
            return note != null ? note.getVersionCount() : 0;
        } catch (Exception e) {
            System.err.println("Errore nel contare le versioni: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }
    
    @Override
    public boolean saveNoteVersion(NoteVersion version) {
        try {
            if (version == null || version.getNoteId() == null) {
                return false;
            }
            
            Note note = noteRepository.getNote(version.getNoteId());
            if (note == null) {
                return false;
            }
            
            note.addVersion(version);
            
            return noteRepository.saveNote(note);
            
        } catch (Exception e) {
            System.err.println("Errore nel salvare la versione: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean deleteAllVersionsForNote(String noteId) {
        try {
            Note note = noteRepository.getNote(noteId);
            if (note == null) {
                return false;
            }
            
            note.getVersions().clear();
            
            return noteRepository.saveNote(note);
            
        } catch (Exception e) {
            System.err.println("Errore nell'eliminare le versioni: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    private String extractNoteIdFromVersionId(String versionId) {
        if (versionId == null || !versionId.contains("_v")) {
            return null;
        }
        
        try {
            return versionId.substring(0, versionId.indexOf("_v"));
        } catch (Exception e) {
            System.err.println("Errore nell'estrazione dell'ID nota dal versionId: " + e.getMessage());
            return null;
        }
    }
}