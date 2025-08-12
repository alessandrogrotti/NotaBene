package com.google.gwt.sample.notabene.server;

import com.google.gwt.sample.notabene.shared.Note;
import com.google.gwt.sample.notabene.shared.NotePermission;
import com.google.gwt.sample.notabene.shared.NoteVersion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Date;
import java.util.List;

public class NoteVersioningTest {
    
    private NoteServiceImpl noteService;
    private NoteVersionServiceImpl versionService;
    private NoteRepository noteRepository;
    
    @BeforeEach
    void setUp() {
        noteRepository = NoteRepository.getInstance();
        noteRepository.clear();
        
        noteService = new NoteServiceImpl();
        versionService = new NoteVersionServiceImpl();
    }
    
    @Test
    void testCreateNoteWithoutVersionHistory() {
        Note note = new Note("Titolo iniziale", "Contenuto iniziale", "testuser");
        note.setPermission(NotePermission.PRIVATE);
        
        assertTrue(noteService.createNote(note));
        
        assertFalse(note.hasVersionHistory());
        assertEquals(0, note.getVersionCount());
        assertEquals(0, note.getVersionNumber());
        
        assertFalse(versionService.hasVersionHistory(note.getId()));
        assertEquals(0, versionService.getVersionCount(note.getId()));
        assertTrue(versionService.getNoteVersions(note.getId()).isEmpty());
    }
    
    @Test
    void testUpdateNoteCreatesVersion() {
        Note note = new Note("Titolo originale", "Contenuto originale", "testuser");
        note.setPermission(NotePermission.PRIVATE);
        assertTrue(noteService.createNote(note));
        
        String noteId = note.getId();
        
        Note updatedNote = noteRepository.getNote(noteId);
        assertNotNull(updatedNote);
        
        updatedNote.setTitle("Titolo modificato");
        updatedNote.setContent("Contenuto modificato");
        
        assertTrue(noteService.updateNote(updatedNote, "testuser"));
        
        Note savedNote = noteRepository.getNote(noteId);
        assertNotNull(savedNote);
        assertTrue(savedNote.hasVersionHistory());
        assertEquals(1, savedNote.getVersionCount());
        assertEquals(1, savedNote.getVersionNumber());
        
        assertTrue(versionService.hasVersionHistory(noteId));
        assertEquals(1, versionService.getVersionCount(noteId));
        
        List<NoteVersion> versions = versionService.getNoteVersions(noteId);
        assertEquals(1, versions.size());
        
        NoteVersion version = versions.get(0);
        assertEquals("Titolo originale", version.getTitle());
        assertEquals("Contenuto originale", version.getContent());
        assertEquals("testuser", version.getEditorUsername());
        assertEquals(0, version.getVersionNumber());
    }
    
    @Test
    void testMultipleUpdatesCreateMultipleVersions() {
        Note note = new Note("Versione 0", "Contenuto v0", "testuser");
        assertTrue(noteService.createNote(note));
        String noteId = note.getId();
        
        Note note1 = noteRepository.getNote(noteId);
        note1.setTitle("Versione 1");
        note1.setContent("Contenuto v1");
        assertTrue(noteService.updateNote(note1, "testuser"));
        
        Note note2 = noteRepository.getNote(noteId);
        note2.setTitle("Versione 2");
        note2.setContent("Contenuto v2");
        assertTrue(noteService.updateNote(note2, "testuser"));
        
        Note note3 = noteRepository.getNote(noteId);
        note3.setTitle("Versione 3");
        note3.setContent("Contenuto v3");
        assertTrue(noteService.updateNote(note3, "testuser"));
        
        Note finalNote = noteRepository.getNote(noteId);
        assertEquals(3, finalNote.getVersionCount());
        assertEquals(3, finalNote.getVersionNumber());
        assertEquals("Versione 3", finalNote.getTitle());
        assertEquals("Contenuto v3", finalNote.getContent());
        
        List<NoteVersion> versions = versionService.getNoteVersions(noteId);
        assertEquals(3, versions.size());
        
        assertEquals("Versione 2", versions.get(0).getTitle());
        assertEquals("testuser", versions.get(0).getEditorUsername());
        assertEquals(2, versions.get(0).getVersionNumber());
        
        assertEquals("Versione 1", versions.get(1).getTitle());
        assertEquals("testuser", versions.get(1).getEditorUsername());
        assertEquals(1, versions.get(1).getVersionNumber());
        
        assertEquals("Versione 0", versions.get(2).getTitle());
        assertEquals("testuser", versions.get(2).getEditorUsername());
        assertEquals(0, versions.get(2).getVersionNumber());
    }
    
    @Test
    void testUpdateWithoutChangesDoesNotCreateVersion() {
        Note note = new Note("Titolo", "Contenuto", "testuser");
        assertTrue(noteService.createNote(note));
        String noteId = note.getId();
        
        Note sameNote = noteRepository.getNote(noteId);
        assertTrue(noteService.updateNote(sameNote, "testuser"));
        
        Note savedNote = noteRepository.getNote(noteId);
        assertFalse(savedNote.hasVersionHistory());
        assertEquals(0, savedNote.getVersionCount());
        assertEquals(0, savedNote.getVersionNumber());
    }
    
    @Test
    void testGetSpecificVersion() {
        Note note = new Note("Titolo originale", "Contenuto originale", "testuser");
        assertTrue(noteService.createNote(note));
        String noteId = note.getId();
        
        Note note1 = noteRepository.getNote(noteId);
        note1.setTitle("Titolo modificato");
        assertTrue(noteService.updateNote(note1, "testuser"));
        
        List<NoteVersion> versions = versionService.getNoteVersions(noteId);
        assertEquals(1, versions.size());
        
        String versionId = versions.get(0).getVersionId();
        NoteVersion specificVersion = versionService.getNoteVersion(versionId);
        
        assertNotNull(specificVersion);
        assertEquals("Titolo originale", specificVersion.getTitle());
        assertEquals("Contenuto originale", specificVersion.getContent());
        assertEquals(0, specificVersion.getVersionNumber());
        assertEquals("testuser", specificVersion.getEditorUsername());
    }
    
    @Test
    void testVersionToReadOnlyNote() {
        Note note = new Note("Titolo", "Contenuto", "testuser");
        note.addTag("tag1");
        note.addTag("tag2");
        assertTrue(noteService.createNote(note));
        
        Note noteToUpdate = noteRepository.getNote(note.getId());
        assertNotNull(noteToUpdate);
        
        noteToUpdate.setTitle("Nuovo titolo");
        assertTrue(noteService.updateNote(noteToUpdate, "testuser"));
        
        List<NoteVersion> versions = versionService.getNoteVersions(note.getId());
        assertEquals(1, versions.size());
        
        NoteVersion version = versions.get(0);
        Note readOnlyNote = version.toReadOnlyNote();
        
        assertEquals("Titolo", readOnlyNote.getTitle());
        assertEquals("Contenuto", readOnlyNote.getContent());
        assertTrue(readOnlyNote.getTags().contains("tag1"));
        assertTrue(readOnlyNote.getTags().contains("tag2"));
        assertEquals("testuser", readOnlyNote.getOwnerUsername());
    }
    
    @Test
    void testVersionCreationDateIsCorrect() throws InterruptedException {
        Note note = new Note("Titolo iniziale", "Contenuto iniziale", "testuser");
        note.setPermission(NotePermission.PRIVATE);
        assertTrue(noteService.createNote(note));
        
        String noteId = note.getId();
        
        Thread.sleep(10);
        
        Note originalNote = noteRepository.getNote(noteId);
        assertNotNull(originalNote);
        Date originalLastModified = originalNote.getLastModified();
        assertNotNull(originalLastModified);
        
        Thread.sleep(10);
        
        Note modifiedNote = new Note("Titolo modificato", "Contenuto modificato", "testuser");
        modifiedNote.setId(noteId);
        modifiedNote.setPermission(originalNote.getPermission());
        
        assertTrue(noteService.updateNote(modifiedNote, "testuser"));
        
        List<NoteVersion> versions = versionService.getNoteVersions(noteId);
        assertEquals(1, versions.size());
        
        NoteVersion version = versions.get(0);
        
        long timeDifference = Math.abs(version.getVersionCreatedAt().getTime() - originalLastModified.getTime());
        assertTrue(timeDifference < 100, // Tolleranza di 100ms
                "La data di creazione della versione (" + version.getVersionCreatedAt().getTime() + 
                ") dovrebbe essere vicina alla data dell'ultima modifica della nota originale (" + 
                originalLastModified.getTime() + "). Differenza: " + timeDifference + "ms");
        
        Note updatedNote = noteRepository.getNote(noteId);
        assertTrue(updatedNote.getLastModified().after(originalLastModified),
                "La data di ultima modifica della nota dovrebbe essere successiva alla data originale");
    }
    
    @Test
    void testMultipleVersionsHaveCorrectDates() throws InterruptedException {
        Note note = new Note("Versione 0", "Contenuto v0", "testuser");
        assertTrue(noteService.createNote(note));
        String noteId = note.getId();
        
        Thread.sleep(10);
        Note note1 = noteRepository.getNote(noteId);
        Date firstModificationTime = note1.getLastModified();
        
        Note modifiedNote1 = new Note("Versione 1", "Contenuto v1", "testuser");
        modifiedNote1.setId(noteId);
        modifiedNote1.setPermission(note1.getPermission());
        assertTrue(noteService.updateNote(modifiedNote1, "testuser"));
        
        Thread.sleep(10);
        Note note2 = noteRepository.getNote(noteId);
        Date secondModificationTime = note2.getLastModified();
        
        Note modifiedNote2 = new Note("Versione 2", "Contenuto v2", "testuser");
        modifiedNote2.setId(noteId);
        modifiedNote2.setPermission(note2.getPermission());
        assertTrue(noteService.updateNote(modifiedNote2, "testuser"));
        
        List<NoteVersion> versions = versionService.getNoteVersions(noteId);
        assertEquals(2, versions.size());
        
        NoteVersion firstVersion = versions.get(0);
        long timeDiff1 = Math.abs(firstVersion.getVersionCreatedAt().getTime() - secondModificationTime.getTime());
        assertTrue(timeDiff1 < 100, // Tolleranza di 100ms
                "La prima versione dovrebbe avere una data vicina alla seconda modifica. " +
                "Atteso: " + secondModificationTime.getTime() + ", Ottenuto: " + firstVersion.getVersionCreatedAt().getTime() +
                ", Differenza: " + timeDiff1 + "ms");
        assertEquals("Versione 1", firstVersion.getTitle());
        
        NoteVersion secondVersion = versions.get(1);
        long timeDiff2 = Math.abs(secondVersion.getVersionCreatedAt().getTime() - firstModificationTime.getTime());
        assertTrue(timeDiff2 < 100, // Tolleranza di 100ms
                "La seconda versione dovrebbe avere una data vicina alla prima modifica. " +
                "Atteso: " + firstModificationTime.getTime() + ", Ottenuto: " + secondVersion.getVersionCreatedAt().getTime() +
                ", Differenza: " + timeDiff2 + "ms");
        assertEquals("Versione 0", secondVersion.getTitle());
    }
    
    @Test
    void testVersionDateWhenNoteHasNoLastModified() {
        Note note = new Note("Titolo", "Contenuto", "testuser");
        note.setPermission(NotePermission.PRIVATE);
        
        Date beforeCreation = new Date();
        
        NoteVersion version = note.createVersion("testuser");
        
        Date afterCreation = new Date();
        
        assertNotNull(version.getVersionCreatedAt());
        assertTrue(version.getVersionCreatedAt().after(beforeCreation) || 
                  version.getVersionCreatedAt().equals(beforeCreation),
                "La data della versione dovrebbe essere >= alla data prima della creazione");
        assertTrue(version.getVersionCreatedAt().before(afterCreation) || 
                  version.getVersionCreatedAt().equals(afterCreation),
                "La data della versione dovrebbe essere <= alla data dopo la creazione");
    }
}