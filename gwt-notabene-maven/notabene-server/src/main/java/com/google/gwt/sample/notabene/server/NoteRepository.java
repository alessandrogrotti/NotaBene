package com.google.gwt.sample.notabene.server;

import com.google.gwt.sample.notabene.shared.Note;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

//Classe responsabile persistenza note
public class NoteRepository {
    
    private static final String NOTES_MAP_NAME = "notes";
    private static NoteRepository instance;
    private DB noteDB;
    private ConcurrentMap<String, Note> notesMap;
    
    private NoteRepository() {
        initializeDatabase();
    }
    
    public static synchronized NoteRepository getInstance() {
        if (instance == null) {
            instance = new NoteRepository();
        }
        return instance;
    }
    
    @SuppressWarnings("unchecked")
    private void initializeDatabase() {
        try {
            noteDB = DBMaker.fileDB(MapDBConstants.NOTEDB_FILENAME)
                    .transactionEnable()
                    .make();

            notesMap = noteDB.treeMap(NOTES_MAP_NAME)
                    .keySerializer(Serializer.STRING)
                    .valueSerializer(Serializer.JAVA)
                    .createOrOpen();
            
            System.out.println("NoteRepository inizializzato con " + notesMap.size() + " note");
        } catch (Exception e) {
            System.err.println("Errore nell'inizializzazione del database delle note: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public boolean saveNote(Note note) {
        try {
            if (note == null || note.getId() == null) {
                return false;
            }
            
            notesMap.put(note.getId(), note);
            noteDB.commit();
            
            System.out.println("Nota salvata con successo: " + note.getId());
            return true;
        } catch (Exception e) {
            System.err.println("Errore nel salvare la nota: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
     public Note getNote(String noteId) {
        try {
            return notesMap.get(noteId);
        } catch (Exception e) {
            System.err.println("Errore nel recuperare la nota: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public boolean noteExists(String noteId) {
        try {
            return notesMap.containsKey(noteId);
        } catch (Exception e) {
            System.err.println("Errore nel controllare l'esistenza della nota: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public void printAllNotes() {
        System.out.println("=== ELENCO NOTE NEL DATABASE ===");
        for (Note note : notesMap.values()) {
            System.out.println("ID: " + note.getId() + 
                             ", Titolo: " + note.getTitle() + 
                             ", Owner: " + note.getOwnerUsername() +
                             ", Tags: " + note.getTags() +
                             ", Permessi: " + note.getPermission());
        }
        System.out.println("===========================");
    }

    //pulisce db, utile per testing
    public void clear() {
        try {
            notesMap.clear();
            noteDB.commit();
        } catch (Exception e) {
            System.err.println("Errore nella pulizia del database: " + e.getMessage());
            noteDB.rollback();
        }
    }

        public List<Note> getUserNotes(String username) {
        try {
            return notesMap.values().stream()
                    .filter(note -> note.getOwnerUsername().equals(username))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Errore nel recuperare le note dell'utente: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<Note> getAccessibleNotes(String username) {
        try {
            return notesMap.values().stream()
                    .filter(note -> note.canRead(username))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Errore nel recuperare le note accessibili all'utente: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
        public boolean deleteNote(String noteId) {
        try {
            Note removed = notesMap.remove(noteId);
            if (removed != null) {
                noteDB.commit();
                System.out.println("Nota eliminata con successo: " + noteId);
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Errore nell'eliminare la nota: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
