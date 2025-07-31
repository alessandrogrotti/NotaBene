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

    /* 
    @BeforeEach
    void setUp() {
        repo = NoteRepository.getInstance();
        repo.clear();
        service = new NoteServiceImpl();
    }


    @Test
    void testCreateNoteSuccess() {
        Note note = new Note("Test Title", "Test Content", "testuser");
        note.setPermission(NotePermission.PRIVATE);
        note.addTag("test");
        note.addTag("junit");
        
        assertTrue(service.createNote(note));
        
        List<Note> userNotes = service.getUserNotes("testuser");
        assertEquals(1, userNotes.size());
        
        Note savedNote = userNotes.get(0);
        assertEquals("Test Title", savedNote.getTitle());
        assertEquals("Test Content", savedNote.getContent());
        assertEquals("testuser", savedNote.getOwnerUsername());
        assertEquals(NotePermission.PRIVATE, savedNote.getPermission());
        assertTrue(savedNote.getTags().contains("test"));
        assertTrue(savedNote.getTags().contains("junit"));
    }

    @Test
    void testCreateNoteWithPermissions() {
        Note note = new Note("Shared Note", "Content", "owner");
        note.setPermission(NotePermission.READ_WRITE);
        note.getReadOnlyUsers().add("reader1");
        note.getWriteUsers().add("writer1");
        
        assertTrue(service.createNote(note));
        
        Note savedNote = service.getNoteById(note.getId(), "owner");
        assertNotNull(savedNote);
        assertEquals(NotePermission.READ_WRITE, savedNote.getPermission());
        assertTrue(savedNote.getReadOnlyUsers().contains("reader1"));
        assertTrue(savedNote.getWriteUsers().contains("writer1"));
    }

    @Test
    void testCreateNoteNull() {
        assertThrows(IllegalArgumentException.class, () -> service.createNote(null));
    }

    @Test
    void testCreateNoteEmptyTitle() {
        Note note = new Note("", "Content", "user");
        assertThrows(IllegalArgumentException.class, () -> service.createNote(note));
    }

    @Test
    void testCreateNoteEmptyContent() {
        Note note = new Note("Title", "", "user");
        assertThrows(IllegalArgumentException.class, () -> service.createNote(note));
    }

    @Test
    void testCreateNoteTitleTooLong() {
        String longTitle = "A".repeat(101);
        Note note = new Note(longTitle, "Content", "user");
        assertThrows(IllegalArgumentException.class, () -> service.createNote(note));
    }

    @Test
    void testCreateNoteContentTooLong() {
        String longContent = "A".repeat(5001);
        Note note = new Note("Title", longContent, "user");
        assertThrows(IllegalArgumentException.class, () -> service.createNote(note));
    }

    @Test
    void testGetUserNotes() {
        // Crea note per diversi utenti
        service.createNote(new Note("Note 1", "Content 1", "user1"));
        service.createNote(new Note("Note 2", "Content 2", "user1"));
        service.createNote(new Note("Note 3", "Content 3", "user2"));
        
        List<Note> user1Notes = service.getUserNotes("user1");
        List<Note> user2Notes = service.getUserNotes("user2");
        
        assertEquals(2, user1Notes.size());
        assertEquals(1, user2Notes.size());
    }

    @Test
    void testNotePermissions() {
        Note privateNote = new Note("Private", "Content", "owner");
        privateNote.setPermission(NotePermission.PRIVATE);
        service.createNote(privateNote);
        
        Note readOnlyNote = new Note("ReadOnly", "Content", "owner");
        readOnlyNote.setPermission(NotePermission.READ_ONLY);
        readOnlyNote.getReadOnlyUsers().add("reader");
        service.createNote(readOnlyNote);
        
        // Owner può sempre leggere
        assertNotNull(service.getNoteById(privateNote.getId(), "owner"));
        assertNotNull(service.getNoteById(readOnlyNote.getId(), "owner"));
        
        // Altri utenti non possono leggere note private
        assertNull(service.getNoteById(privateNote.getId(), "other"));
        
        // Reader può leggere note read-only
        assertNotNull(service.getNoteById(readOnlyNote.getId(), "reader"));
    }
        */
}
