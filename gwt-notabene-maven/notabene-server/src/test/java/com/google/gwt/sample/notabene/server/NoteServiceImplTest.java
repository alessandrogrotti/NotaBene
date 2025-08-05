package com.google.gwt.sample.notabene.server;

import com.google.gwt.sample.notabene.shared.Note;
import com.google.gwt.sample.notabene.shared.NotePermission;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class NoteServiceImplTest {
    private NoteServiceImpl service;
    private NoteRepository repo;

    
    @BeforeEach
    void setUp() {
        repo = NoteRepository.getInstance();
        repo.clear();
        service = new NoteServiceImpl();
    }


    @Test
    void testCreateNoteSuccess() {
        Note note = new Note("Test titolo", "Testo di prova", "testuser");
        note.setPermission(NotePermission.PRIVATE);
        note.addTag("prova1");
        note.addTag("prova2");
        
        assertTrue(service.createNote(note));
        
        List<Note> userNotes = service.getUserNotes("testuser");
        assertEquals(1, userNotes.size());
        
        Note savedNote = userNotes.get(0);
        assertEquals("Test titolo", savedNote.getTitle());
        assertEquals("Testo di prova", savedNote.getContent());
        assertEquals("testuser", savedNote.getOwnerUsername());
        assertEquals(NotePermission.PRIVATE, savedNote.getPermission());
        assertTrue(savedNote.getTags().contains("prova1"));
        assertTrue(savedNote.getTags().contains("prova2"));
    }

    @Test
    void testCreateNoteWithPermissions() {
        Note note = new Note("Nota condivisa", "testo", "owner");
        note.setPermission(NotePermission.READ_WRITE);
        note.getReadOnlyUsers().add("lettore1");
        note.getWriteUsers().add("scrittore1");
        
        assertTrue(service.createNote(note));
        
        Note savedNote = service.getNoteById(note.getId(), "owner");
        assertNotNull(savedNote);
        assertEquals(NotePermission.READ_WRITE, savedNote.getPermission());
        assertTrue(savedNote.getReadOnlyUsers().contains("lettore1"));
        assertTrue(savedNote.getWriteUsers().contains("scrittore1"));
    }

    @Test
    void testCreateNoteNull() {
        assertThrows(IllegalArgumentException.class, () -> service.createNote(null));
    }

    @Test
    void testCreateNoteEmptyTitle() {
        Note note = new Note("", "testo di prova", "user");
        assertThrows(IllegalArgumentException.class, () -> service.createNote(note));
    }

    @Test
    void testCreateNoteEmptyContent() {
        Note note = new Note("Titolo", "", "user");
        assertThrows(IllegalArgumentException.class, () -> service.createNote(note));
    }

    @Test
    void testCreateNoteTitleTooLong() {
        String longTitle = "A".repeat(101);
        Note note = new Note(longTitle, "Contenuto di prova", "user");
        assertThrows(IllegalArgumentException.class, () -> service.createNote(note));
    }

    @Test
    void testCreateNoteContentTooLong() {
        String longContent = "A".repeat(281);
        Note note = new Note("Titolo", longContent, "user");
        assertThrows(IllegalArgumentException.class, () -> service.createNote(note));
    }

    @Test
    void testGetUserNotes() {
        // Crea note per diversi utenti
        service.createNote(new Note("Nota 1", "Contenuto 1", "user1"));
        service.createNote(new Note("Nota 2", "Contenuto 2", "user1"));
        service.createNote(new Note("Nota 3", "Contenuto 3", "user2"));

        List<Note> user1Notes = service.getUserNotes("user1");
        List<Note> user2Notes = service.getUserNotes("user2");
        
        assertEquals(2, user1Notes.size());
        assertEquals(1, user2Notes.size());
    }

    @Test
    void testNotePermissions() {
        Note privateNote = new Note("Nota Privata", "Contenuto", "owner");
        privateNote.setPermission(NotePermission.PRIVATE);
        service.createNote(privateNote);
        
        Note readOnlyNote = new Note("Nota Sola Lettura", "Contenuto", "owner");
        readOnlyNote.setPermission(NotePermission.READ_ONLY);
        readOnlyNote.getReadOnlyUsers().add("lettore");
        service.createNote(readOnlyNote);
        
        assertNotNull(service.getNoteById(privateNote.getId(), "owner"));
        assertNotNull(service.getNoteById(readOnlyNote.getId(), "owner"));
        
        assertNull(service.getNoteById(privateNote.getId(), "owner2"));
        
        assertNotNull(service.getNoteById(readOnlyNote.getId(), "lettore"));
    }
    @Test
    void testDeleteNoteSuccess() {
        Note note = new Note("Nota da eliminare", "Contenuto di prova", "testuser");
        service.createNote(note);
        String noteId = note.getId();
        
        // verifica esistenza nota di prova
        assertNotNull(service.getNoteById(noteId, "testuser"));
        assertEquals(1, service.getUserNotes("testuser").size());
        boolean result = service.deleteNote(noteId, "testuser");
        assertTrue(result);
        assertNull(service.getNoteById(noteId, "testuser"));
        assertEquals(0, service.getUserNotes("testuser").size());
    }

  
    @Test
    void testDeleteMultipleNotes() {
        Note note1 = new Note("Nota 1", "Contenuto 1", "testuser");
        note1.setId("note1");
        Note note2 = new Note("Nota 2", "Contenuto 2", "testuser");
        note2.setId("note2");   
        Note note3 = new Note("Nota 3", "Contenuto 3", "altrouser");
        note3.setId("note3");

        service.createNote(note1);
        service.createNote(note2);
        service.createNote(note3);
        
        assertEquals(2, service.getUserNotes("testuser").size());
        assertEquals(1, service.getUserNotes("altrouser").size());

        boolean result = service.deleteNote(note1.getId(), "testuser");
        
        // solo la nota1 deve essere eliminata
        assertTrue(result);
        assertEquals(1, service.getUserNotes("testuser").size());
        assertEquals(1, service.getUserNotes("altrouser").size());
        
        // verifichiamo che rimanga solo note2 per testuser
        List<Note> remainingNotes = service.getUserNotes("testuser");
        assertEquals("Nota 2", remainingNotes.get(0).getTitle());
        
        // verifichiamo che note3 di altrouser sia ancora presente
        assertNotNull(service.getNoteById(note3.getId(), "altrouser"));
    }


    @Test
    void testDeleteNoteUpdatesAccessibleNotes() {
        Note privateNote = new Note("Nota privata", "Contenuto privato", "user1");
        privateNote.setPermission(NotePermission.PRIVATE);
        privateNote.setId("private_note_1");
        
        Note sharedNote = new Note("Nota condivisa", "Contenuto condiviso", "user1");
        sharedNote.setPermission(NotePermission.READ_ONLY);
        sharedNote.getReadOnlyUsers().add("user2");
        sharedNote.setId("shared_note_1");
        
        service.createNote(privateNote);
        service.createNote(sharedNote);
        //stato iniziale
        assertEquals(2, service.getUserNotes("user1").size());
        assertEquals(0, service.getUserNotes("user2").size());
        assertEquals(1, service.getAccessibleNotes("user2").size());
        
        //eliminiamo nota condivisa
        boolean result = service.deleteNote(sharedNote.getId(), "user1");
        
        // verifichiamo che l'eliminazione influenzi correttamente le note accessibili
        assertTrue(result);
        assertEquals(1, service.getUserNotes("user1").size());
        assertEquals(0, service.getUserNotes("user2").size());
        assertEquals(0, service.getAccessibleNotes("user2").size());
    }
        
}
