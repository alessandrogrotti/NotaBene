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
        
}
