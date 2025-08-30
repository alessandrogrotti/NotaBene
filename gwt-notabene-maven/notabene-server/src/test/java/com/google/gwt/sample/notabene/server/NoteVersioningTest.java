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
    private NoteRepository repo;
    
    @BeforeEach
    void setUp() {
        repo = NoteRepository.getInstance();
        repo.clear();
        
        noteService = new NoteServiceImpl();
        versionService = new NoteVersionServiceImpl();
    }
    
    @Test
    void testCreateNoteWithoutVersionHistory() {
        Note note = new Note("Appunti iniziali", "Contenuto delle note", "mario");
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
        Note note = new Note("Lista spesa", "Latte e pane", "giulia");
        note.setPermission(NotePermission.PRIVATE);
        assertTrue(noteService.createNote(note));
        
        String noteId = note.getId();
        
        // Modifica la nota esistente
        note.setTitle("Lista spesa aggiornata");
        note.setContent("Latte, pane e uova");
        
        assertTrue(noteService.updateNote(note, "giulia"));
        
        Note savedNote = noteService.getNoteById(noteId, "giulia");
        assertNotNull(savedNote);
        assertTrue(savedNote.hasVersionHistory());
        assertEquals(1, savedNote.getVersionCount());
        assertEquals(1, savedNote.getVersionNumber());
        
        assertTrue(versionService.hasVersionHistory(noteId));
        assertEquals(1, versionService.getVersionCount(noteId));
        
        List<NoteVersion> versions = versionService.getNoteVersions(noteId);
        assertEquals(1, versions.size());
        
        NoteVersion version = versions.get(0);
        assertEquals("Lista spesa", version.getTitle());
        assertEquals("Latte e pane", version.getContent());
        assertEquals("giulia", version.getEditorUsername());
        assertEquals(0, version.getVersionNumber());
    }
    
    @Test
    void testMultipleUpdatesCreateMultipleVersions() {
        Note note = new Note("Ricette dolci", "Tiramisu", "anna");
        note.setPermission(NotePermission.PRIVATE);
        assertTrue(noteService.createNote(note));
        String noteId = note.getId();
        
        // Prima modifica
        note.setTitle("Ricette dolci v1");
        note.setContent("Tiramisu e crostata");
        assertTrue(noteService.updateNote(note, "anna"));
        
        // Seconda modifica  
        note.setTitle("Ricette dolci v2");
        note.setContent("Tiramisu, crostata e cannoli");
        assertTrue(noteService.updateNote(note, "anna"));
        
        // Terza modifica
        note.setTitle("Ricette dolci complete");
        note.setContent("Tiramisu, crostata, cannoli e gelato");
        assertTrue(noteService.updateNote(note, "anna"));
        
        Note finalNote = noteService.getNoteById(noteId, "anna");
        assertEquals(3, finalNote.getVersionCount());
        assertEquals(3, finalNote.getVersionNumber());
        assertEquals("Ricette dolci complete", finalNote.getTitle());
        assertEquals("Tiramisu, crostata, cannoli e gelato", finalNote.getContent());
        
        List<NoteVersion> versions = versionService.getNoteVersions(noteId);
        assertEquals(3, versions.size());
        
        // Le versioni sono ordinate dalla più recente alla più vecchia
        assertEquals("Ricette dolci v2", versions.get(0).getTitle());
        assertEquals("anna", versions.get(0).getEditorUsername());
        assertEquals(2, versions.get(0).getVersionNumber());
        
        assertEquals("Ricette dolci v1", versions.get(1).getTitle());
        assertEquals("anna", versions.get(1).getEditorUsername());
        assertEquals(1, versions.get(1).getVersionNumber());
        
        assertEquals("Ricette dolci", versions.get(2).getTitle());
        assertEquals("anna", versions.get(2).getEditorUsername());
        assertEquals(0, versions.get(2).getVersionNumber());
    }
    
    @Test
    void testUpdateWithoutChangesDoesNotCreateVersion() {
        Note note = new Note("Appunti matematica", "Formule trigonometriche", "luca");
        note.setPermission(NotePermission.PRIVATE);
        assertTrue(noteService.createNote(note));
        String noteId = note.getId();
        
        // Aggiorna con gli stessi valori (nessuna modifica effettiva)
        Note sameNote = new Note("Appunti matematica", "Formule trigonometriche", "luca");
        sameNote.setId(noteId);
        sameNote.setPermission(NotePermission.PRIVATE);
        
        assertTrue(noteService.updateNote(sameNote, "luca"));
        
        Note savedNote = noteService.getNoteById(noteId, "luca");
        assertFalse(savedNote.hasVersionHistory());
        assertEquals(0, savedNote.getVersionCount());
        assertEquals(0, savedNote.getVersionNumber());
    }
    
    @Test
    void testGetSpecificVersion() {
        Note note = new Note("Progetto software", "Requisiti iniziali", "marco");
        note.setPermission(NotePermission.PRIVATE);
        assertTrue(noteService.createNote(note));
        String noteId = note.getId();
        
        // Modifica la nota
        note.setTitle("Progetto software - analisi");
        note.setContent("Requisiti e casi d'uso");
        assertTrue(noteService.updateNote(note, "marco"));
        
        List<NoteVersion> versions = versionService.getNoteVersions(noteId);
        assertEquals(1, versions.size());
        
        String versionId = versions.get(0).getVersionId();
        NoteVersion specificVersion = versionService.getNoteVersion(versionId);
        
        assertNotNull(specificVersion);
        assertEquals("Progetto software", specificVersion.getTitle());
        assertEquals("Requisiti iniziali", specificVersion.getContent());
        assertEquals(0, specificVersion.getVersionNumber());
        assertEquals("marco", specificVersion.getEditorUsername());
    }
    
    @Test
    void testVersionToReadOnlyNote() {
        Note note = new Note("Tutorial Java", "Concetti base OOP", "paolo");
        note.setPermission(NotePermission.PRIVATE);
        note.addTag("java");
        note.addTag("programmazione");
        assertTrue(noteService.createNote(note));
        
        // Modifica la nota
        note.setTitle("Tutorial Java avanzato");
        note.setContent("Concetti avanzati di OOP");
        assertTrue(noteService.updateNote(note, "paolo"));
        
        List<NoteVersion> versions = versionService.getNoteVersions(note.getId());
        assertEquals(1, versions.size());
        
        NoteVersion version = versions.get(0);
        Note readOnlyNote = version.toReadOnlyNote();
        
        assertEquals("Tutorial Java", readOnlyNote.getTitle());
        assertEquals("Concetti base OOP", readOnlyNote.getContent());
        assertTrue(readOnlyNote.getTags().contains("java"));
        assertTrue(readOnlyNote.getTags().contains("programmazione"));
        assertEquals("paolo", readOnlyNote.getOwnerUsername());
    }
    
    @Test
    void testVersionCreationDateExists() {
        Note note = new Note("Appunti storia", "Rivoluzione francese", "sara");
        note.setPermission(NotePermission.PRIVATE);
        assertTrue(noteService.createNote(note));
        
        // Modifica la nota
        note.setTitle("Appunti storia moderna");
        note.setContent("Rivoluzione francese e napoleonica");
        assertTrue(noteService.updateNote(note, "sara"));
        
        List<NoteVersion> versions = versionService.getNoteVersions(note.getId());
        assertEquals(1, versions.size());
        
        NoteVersion version = versions.get(0);
        assertNotNull(version.getVersionCreatedAt());
        
        Note updatedNote = noteService.getNoteById(note.getId(), "sara");
        assertNotNull(updatedNote.getLastModified());
        assertTrue(updatedNote.getLastModified().after(version.getVersionCreatedAt()) ||
                  updatedNote.getLastModified().equals(version.getVersionCreatedAt()));
    }
    
    @Test
    void testVersionCreationWithTags() {
        Note note = new Note("Appunti linguaggi", "Java e Python", "lucia");
        note.setPermission(NotePermission.PRIVATE);
        note.addTag("programmazione");
        note.addTag("java");
        assertTrue(noteService.createNote(note));
        
        // Modifica titolo e aggiungi tag
        note.setTitle("Appunti linguaggi completi");
        note.addTag("python");
        assertTrue(noteService.updateNote(note, "lucia"));
        
        List<NoteVersion> versions = versionService.getNoteVersions(note.getId());
        assertEquals(1, versions.size());
        
        NoteVersion version = versions.get(0);
        assertEquals("Appunti linguaggi", version.getTitle());
        assertEquals("Java e Python", version.getContent());
        assertTrue(version.getTags().contains("programmazione"));
        assertTrue(version.getTags().contains("java"));
        assertFalse(version.getTags().contains("python")); // Tag aggiunto dopo
    }
    
    @Test
    void testUpdateNoteNull() {
        assertFalse(noteService.updateNote(null, "mario"));
    }
    
    @Test
    void testVersionServiceWithNullNoteId() {
        assertFalse(versionService.hasVersionHistory(null));
        assertEquals(0, versionService.getVersionCount(null));
        assertTrue(versionService.getNoteVersions(null).isEmpty());
    }
    
    @Test
    void testVersionServiceWithNonExistentNoteId() {
        String fakeId = "nota_inesistente_123";
        assertFalse(versionService.hasVersionHistory(fakeId));
        assertEquals(0, versionService.getVersionCount(fakeId));
        assertTrue(versionService.getNoteVersions(fakeId).isEmpty());
    }
    
    @Test
    void testUpdateNoteByWriteUserCreatesVersion() {
        Note note = new Note("Documento condiviso", "Versione iniziale", "mario");
        note.setPermission(NotePermission.READ_WRITE);
        note.getWriteUsers().add("luigi");
        assertTrue(noteService.createNote(note));
        
        // Luigi modifica la nota
        note.setTitle("Documento condiviso v2");
        note.setContent("Versione modificata da Luigi");
        assertTrue(noteService.updateNote(note, "luigi"));
        
        List<NoteVersion> versions = versionService.getNoteVersions(note.getId());
        assertEquals(1, versions.size());
        
        NoteVersion version = versions.get(0);
        assertEquals("Documento condiviso", version.getTitle());
        assertEquals("Versione iniziale", version.getContent());
        assertEquals("mario", version.getEditorUsername()); // Editore originale
    }
    
    @Test
    void testUpdateNoteByReadOnlyUserDoesNotCreateVersion() {
        Note note = new Note("Documento sola lettura", "Contenuto originale", "mario");
        note.setPermission(NotePermission.READ_ONLY);
        note.getReadOnlyUsers().add("luigi");
        assertTrue(noteService.createNote(note));
        
        // Luigi tenta di modificare (non dovrebbe riuscire)
        note.setTitle("Documento modificato");
        assertFalse(noteService.updateNote(note, "luigi"));
        
        // Nessuna versione creata
        List<NoteVersion> versions = versionService.getNoteVersions(note.getId());
        assertEquals(0, versions.size());
    }
    
    @Test
    void testDeleteNoteWithVersionsRemovesVersionHistory() {
        Note note = new Note("Nota da cancellare", "Contenuto originale", "anna");
        note.setPermission(NotePermission.PRIVATE);
        assertTrue(noteService.createNote(note));
        
        // Crea alcune versioni
        note.setTitle("Nota modificata 1");
        assertTrue(noteService.updateNote(note, "anna"));
        
        note.setTitle("Nota modificata 2");
        assertTrue(noteService.updateNote(note, "anna"));
        
        // Verifica che ci siano versioni
        assertEquals(2, versionService.getVersionCount(note.getId()));
        
        // Elimina la nota
        assertTrue(noteService.deleteNote(note.getId(), "anna"));
        
        // Verifica che la nota e le versioni siano sparite
        assertNull(noteService.getNoteById(note.getId(), "anna"));
        assertEquals(0, versionService.getVersionCount(note.getId()));
        assertTrue(versionService.getNoteVersions(note.getId()).isEmpty());
    }
}