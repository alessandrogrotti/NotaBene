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
        String longTitle = "A".repeat(51);
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
    
    @Test
    void testUpdateNoteTitleAndContent() {
        Note note = new Note("Titolo iniziale", "Contenuto iniziale", "mario");
        note.setPermission(NotePermission.PRIVATE);
        service.createNote(note);

        note.setTitle("Titolo modificato");
        note.setContent("Contenuto modificato");
        boolean result = service.updateNote(note, "mario");
        assertTrue(result);

        Note updated = service.getNoteById(note.getId(), "mario");
        assertEquals("Titolo modificato", updated.getTitle());
        assertEquals("Contenuto modificato", updated.getContent());
    }

    @Test
    void testUpdateNoteTags() {
        Note note = new Note("Titolo", "Contenuto", "mario");
        note.setPermission(NotePermission.PRIVATE);
        note.addTag("vecchioTag");
        service.createNote(note);

        note.getTags().clear();
        note.addTag("nuovoTag");
        boolean result = service.updateNote(note, "mario");
        assertTrue(result);

        Note updated = service.getNoteById(note.getId(), "mario");
        assertTrue(updated.getTags().contains("nuovoTag"));
        assertFalse(updated.getTags().contains("vecchioTag"));
    }

    @Test
    void testUpdateNotePermissionsAndUsers() {
        Note note = new Note("Titolo", "Contenuto", "mario");
        note.setPermission(NotePermission.PRIVATE);
        service.createNote(note);

        note.setPermission(NotePermission.READ_WRITE);
        note.getReadOnlyUsers().add("lettore");
        note.getWriteUsers().add("scrittore");
        boolean result = service.updateNote(note, "mario");
        assertTrue(result);

        Note updated = service.getNoteById(note.getId(), "mario");
        assertEquals(NotePermission.READ_WRITE, updated.getPermission());
        assertTrue(updated.getReadOnlyUsers().contains("lettore"));
        assertTrue(updated.getWriteUsers().contains("scrittore"));
    }

    @Test
    void testUpdateNoteByNonOwnerCannotChangePermissions() {
        Note note = new Note("Titolo", "Contenuto", "mario");
        note.setPermission(NotePermission.PRIVATE);
        service.createNote(note);

        note.setPermission(NotePermission.READ_WRITE);
        note.getReadOnlyUsers().add("lettore");
        note.getWriteUsers().add("scrittore");
        boolean result = service.updateNote(note, "altroUtente");
        assertFalse(result); 

        Note updated = service.getNoteById(note.getId(), "mario");
        assertEquals(NotePermission.PRIVATE, updated.getPermission());
        assertTrue(updated.getReadOnlyUsers().isEmpty());
        assertTrue(updated.getWriteUsers().isEmpty());
    }

    @Test
    void testUpdateNoteNonExistent() {
        Note note = new Note("Titolo", "Contenuto", "mario");
        note.setId("nota_inesistente");
        boolean result = service.updateNote(note, "mario");
        assertFalse(result);
    }
    
    @Test
    void testDuplicateNoteSuccess() {
        Note originalNote = new Note("Ricette dolci", "Tiramisu, panna cotta, crostata", "anna");
        originalNote.addTag("cucina");
        originalNote.addTag("dolci");
        originalNote.setPermission(NotePermission.PRIVATE);
        service.createNote(originalNote);
        
        // Duplica la nota
        Note duplicatedNote = service.duplicateNote(originalNote.getId(), "anna");
        
        assertNotNull(duplicatedNote);
        assertNotEquals(originalNote.getId(), duplicatedNote.getId()); 
        assertEquals("Ricette dolci", duplicatedNote.getTitle()); 
        assertEquals(originalNote.getContent(), duplicatedNote.getContent()); 
        assertEquals("anna", duplicatedNote.getOwnerUsername()); 
        assertEquals(originalNote.getTags(), duplicatedNote.getTags()); 
        assertEquals(NotePermission.PRIVATE, duplicatedNote.getPermission()); 
        assertEquals(0, duplicatedNote.getVersionNumber()); 
        
        // Verifica che ci siano ora 2 note per l'utente
        List<Note> userNotes = service.getUserNotes("anna");
        assertEquals(2, userNotes.size());
    }
    
    @Test
    void testDuplicateNoteByDifferentUser() {
        Note originalNote = new Note("Ricette salate", "Pasta al pomodoro, risotto", "anna");
        originalNote.setPermission(NotePermission.READ_ONLY);
        originalNote.getReadOnlyUsers().add("luca"); 
        service.createNote(originalNote);
        
        Note duplicatedNote = service.duplicateNote(originalNote.getId(), "luca");
        
        assertNotNull(duplicatedNote);
        assertNotEquals(originalNote.getId(), duplicatedNote.getId());
        assertEquals("Ricette salate", duplicatedNote.getTitle());
        assertEquals(originalNote.getContent(), duplicatedNote.getContent());
        assertEquals("luca", duplicatedNote.getOwnerUsername()); 
        assertEquals(NotePermission.PRIVATE, duplicatedNote.getPermission()); 
            
        // Verifica che il primo utente abbia ancora la sua nota
        List<Note> annaNotes = service.getUserNotes("anna");
        assertEquals(1, annaNotes.size());
        
        // Verifica che il secondo utente abbia la sua copia
        List<Note> lucaNotes = service.getUserNotes("luca");
        assertEquals(1, lucaNotes.size());
    }
    
    @Test
    void testDuplicateSharedNote() {
        Note originalNote = new Note("Appunti lavoro", "Meeting ore 15, deadline progetto", "luca");
        originalNote.setPermission(NotePermission.READ_WRITE);
        originalNote.getWriteUsers().add("anna");
        service.createNote(originalNote);
        
        Note duplicatedNote = service.duplicateNote(originalNote.getId(), "anna");
        
        assertNotNull(duplicatedNote);
        assertEquals("anna", duplicatedNote.getOwnerUsername()); 
        assertEquals(NotePermission.PRIVATE, duplicatedNote.getPermission());
        assertTrue(duplicatedNote.getReadOnlyUsers().isEmpty()); 
        assertTrue(duplicatedNote.getWriteUsers().isEmpty());
        
        Note originalAfterDuplicate = service.getNoteById(originalNote.getId(), "luca");
        assertEquals(NotePermission.READ_WRITE, originalAfterDuplicate.getPermission());
        assertTrue(originalAfterDuplicate.getWriteUsers().contains("anna"));
    }
    
    @Test
    void testDuplicateNoteWithoutPermission() {
        Note privateNote = new Note("Nota privata", "Contenuto segreto", "anna");
        privateNote.setPermission(NotePermission.PRIVATE);
        service.createNote(privateNote);
        
        // il secondo utente tenta di duplicare la nota privata del primo utente
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            service.duplicateNote(privateNote.getId(), "luca");
        });
        
        assertEquals("Non hai i permessi per leggere questa nota", exception.getMessage());
    }
    
    @Test
    void testDuplicateNoteNotFound() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            service.duplicateNote("nota_inesistente", "anna");
        });
        
        assertEquals("Nota non trovata", exception.getMessage());
    }
    
    @Test
    void testDuplicateNoteWithNullParameters() {
        IllegalArgumentException exception1 = assertThrows(IllegalArgumentException.class, () -> {
            service.duplicateNote(null, "anna");
        });
        assertEquals("ID della nota e username sono obbligatori", exception1.getMessage());
        
        IllegalArgumentException exception2 = assertThrows(IllegalArgumentException.class, () -> {
            service.duplicateNote("validId", null);
        });
        assertEquals("ID della nota e username sono obbligatori", exception2.getMessage());
        
        IllegalArgumentException exception3 = assertThrows(IllegalArgumentException.class, () -> {
            service.duplicateNote("", "anna");
        });
        assertEquals("ID della nota e username sono obbligatori", exception3.getMessage());
    }
    
    @Test
    void testRemoveUserFromNoteSuccess() {
        Note note = new Note("Nota condivisa", "Contenuto condiviso", "owner");
        note.setPermission(NotePermission.READ_WRITE);
        note.getReadOnlyUsers().add("lettore1");
        note.getWriteUsers().add("scrittore1");
        note.getWriteUsers().add("scrittore2");
        service.createNote(note);
        
        boolean result = service.removeUserFromNote(note.getId(), "scrittore1", "scrittore1");
        
        assertTrue(result);
        
        Note updated = service.getNoteById(note.getId(), "owner");
        assertFalse(updated.getWriteUsers().contains("scrittore1"));
        assertTrue(updated.getWriteUsers().contains("scrittore2"));
        assertTrue(updated.getReadOnlyUsers().contains("lettore1"));
    }
    
    @Test
    void testRemoveUserFromNoteReadOnlySuccess() {
        Note note = new Note("Nota sola lettura", "Contenuto", "owner");
        note.setPermission(NotePermission.READ_ONLY);
        note.getReadOnlyUsers().add("lettore1");
        note.getReadOnlyUsers().add("lettore2");
        service.createNote(note);
        
        boolean result = service.removeUserFromNote(note.getId(), "lettore1", "lettore1");
        
        assertTrue(result);
        
        Note updated = service.getNoteById(note.getId(), "owner");
        assertFalse(updated.getReadOnlyUsers().contains("lettore1"));
        assertTrue(updated.getReadOnlyUsers().contains("lettore2"));
    }
    
    @Test
    void testRemoveUserFromNoteInvalidNoteId() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            service.removeUserFromNote(null, "user", "requester");
        });
        assertEquals("L'ID della nota è obbligatorio", exception.getMessage());
        
        exception = assertThrows(IllegalArgumentException.class, () -> {
            service.removeUserFromNote("", "user", "requester");
        });
        assertEquals("L'ID della nota è obbligatorio", exception.getMessage());
    }
    
    @Test
    void testRemoveUserFromNoteInvalidRequester() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            service.removeUserFromNote("noteId", "user", null);
        });
        assertEquals("L'username richiedente è obbligatorio", exception.getMessage());
        
        exception = assertThrows(IllegalArgumentException.class, () -> {
            service.removeUserFromNote("noteId", "user", "");
        });
        assertEquals("L'username richiedente è obbligatorio", exception.getMessage());
    }
    
    @Test
    void testRemoveUserFromNoteNotFound() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            service.removeUserFromNote("nota_inesistente", "user", "requester");
        });
        assertEquals("Nota non trovata", exception.getMessage());
    }
    
    @Test
    void testRemoveOwnerFromNote() {
        Note note = new Note("Nota del proprietario", "Contenuto", "owner");
        note.setPermission(NotePermission.READ_WRITE);
        service.createNote(note);
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            service.removeUserFromNote(note.getId(), "owner", "owner");
        });
        assertEquals("Il proprietario della nota non può rimuovere se stesso", exception.getMessage());
    }
    
    @Test
    void testRemoveUserFromNoteUserHasNoPermissions() {
        Note note = new Note("Nota condivisa", "Contenuto", "owner");
        note.setPermission(NotePermission.READ_WRITE);
        note.getWriteUsers().add("scrittore1");
        service.createNote(note);
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            service.removeUserFromNote(note.getId(), "utente_senza_permessi", "owner");
        });
        assertEquals("L'utente non ha permessi su questa nota", exception.getMessage());
    }
    
    @Test
    void testRemoveUserFromNoteBothReadAndWrite() {
        Note note = new Note("Nota con permessi misti", "Contenuto", "owner");
        note.setPermission(NotePermission.READ_WRITE);
        note.getReadOnlyUsers().add("utente1");
        note.getWriteUsers().add("utente1"); 
        service.createNote(note);
        
        boolean result = service.removeUserFromNote(note.getId(), "utente1", "utente1");
        
        assertTrue(result);
        
        Note updated = service.getNoteById(note.getId(), "owner");
        assertFalse(updated.getReadOnlyUsers().contains("utente1"));
        assertFalse(updated.getWriteUsers().contains("utente1"));
    }
    
    @Test
    void testRemoveUserFromNoteAffectsAccessibility() {
        Note note = new Note("Nota condivisa", "Contenuto", "owner");
        note.setPermission(NotePermission.READ_ONLY);
        note.getReadOnlyUsers().add("lettore1");
        service.createNote(note);
        
        List<Note> accessibleNotes = service.getAccessibleNotes("lettore1");
        assertEquals(1, accessibleNotes.size());
        
        boolean result = service.removeUserFromNote(note.getId(), "lettore1", "lettore1");
        assertTrue(result);
        
        accessibleNotes = service.getAccessibleNotes("lettore1");
        assertEquals(0, accessibleNotes.size());
        
        Note retrievedNote = service.getNoteById(note.getId(), "lettore1");
        assertNull(retrievedNote);
    }

  
}
