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
        Note note = new Note("Appunti Java", "Concetti base di programmazione", "mario");
        note.setPermission(NotePermission.PRIVATE);
        note.addTag("java");
        note.addTag("programmazione");
        
        assertTrue(service.createNote(note));
        
        List<Note> userNotes = service.getUserNotes("mario");
        assertEquals(1, userNotes.size());
        
        Note savedNote = userNotes.get(0);
        assertEquals("Appunti Java", savedNote.getTitle());
        assertEquals("Concetti base di programmazione", savedNote.getContent());
        assertEquals("mario", savedNote.getOwnerUsername());
        assertEquals(NotePermission.PRIVATE, savedNote.getPermission());
        assertTrue(savedNote.getTags().contains("java"));
        assertTrue(savedNote.getTags().contains("programmazione"));
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
        Note note = new Note("", "contenuto della nota", "marco");
        assertThrows(IllegalArgumentException.class, () -> service.createNote(note));
    }

    @Test
    void testCreateNoteEmptyContent() {
        Note note = new Note("Titolo nota", "", "marco");
        assertThrows(IllegalArgumentException.class, () -> service.createNote(note));
    }

    @Test
    void testCreateNoteTitleTooLong() {
        String longTitle = "A".repeat(101);
        Note note = new Note(longTitle, "Contenuto valido", "marco");
        assertThrows(IllegalArgumentException.class, () -> service.createNote(note));
    }

    @Test
    void testCreateNoteContentTooLong() {
        String longContent = "A".repeat(281);
        Note note = new Note("Titolo valido", longContent, "marco");
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
        // Arrange - Crea una nota
        Note note = new Note("Lista spesa", "Latte, pane, uova", "giulia");
        service.createNote(note);
        String noteId = note.getId();
        
        // Verifica che la nota esista
        assertNotNull(service.getNoteById(noteId, "giulia"));
        assertEquals(1, service.getUserNotes("giulia").size());
        
        // Act - Elimina la nota
        boolean result = service.deleteNote(noteId, "giulia");
        
        // Assert - Verifica che l'eliminazione sia avvenuta con successo
        assertTrue(result);
        assertNull(service.getNoteById(noteId, "giulia"));
        assertEquals(0, service.getUserNotes("giulia").size());
    }

  
    @Test
    void testDeleteMultipleNotes() {
        // Arrange - Crea multiple note
        Note note1 = new Note("Ricette dolci", "Tiramisu e crostata", "anna");
        Note note2 = new Note("Ricette salate", "Pasta e risotto", "anna");
        Note note3 = new Note("Appunti lavoro", "Meeting di lunedì", "luca");
        
        service.createNote(note1);
        service.createNote(note2);
        service.createNote(note3);
        
        assertEquals(2, service.getUserNotes("anna").size());
        assertEquals(1, service.getUserNotes("luca").size());
        
        // Act - Elimina solo una nota di anna
        boolean result = service.deleteNote(note1.getId(), "anna");
        
        // Assert - Solo la nota1 deve essere eliminata
        assertTrue(result);
        assertEquals(1, service.getUserNotes("anna").size());
        assertEquals(1, service.getUserNotes("luca").size());
        
        // Verifica che rimanga solo note2 per anna
        List<Note> remainingNotes = service.getUserNotes("anna");
        assertEquals("Ricette salate", remainingNotes.get(0).getTitle());
        
        // Verifica che note3 di luca sia ancora presente
        assertNotNull(service.getNoteById(note3.getId(), "luca"));
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
