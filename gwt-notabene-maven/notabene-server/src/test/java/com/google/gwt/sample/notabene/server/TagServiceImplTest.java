package com.google.gwt.sample.notabene.server;

import com.google.gwt.sample.notabene.shared.Tag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TagServiceImplTest {
    private TagServiceImpl service;
    private TagRepository repo;

    @BeforeEach
    void setUp() {
        repo = TagRepository.getInstance();
        repo.clear();
        service = new TagServiceImpl();
    }

    @Test
    void testCreateTagSuccess() {
        Tag tag = new Tag("Programmazione");
        
        assertTrue(service.createTag(tag));
        assertTrue(service.isTagNameExists("Programmazione"));
        
        List<Tag> allTags = service.getAllTags();
        assertEquals(1, allTags.size());
        
        Tag savedTag = allTags.get(0);
        assertEquals("Programmazione", savedTag.getName());
    }

    @Test
    void testCreateTagEmptyName() {
        Tag tag = new Tag("");
        assertFalse(service.createTag(tag));
    }

    @Test //<2 caratteri
    void testCreateTagShortName() {
        Tag tag = new Tag("A");
        assertFalse(service.createTag(tag));
    }

    @Test //>30 caratteri
    void testCreateTagLongName() {
        String longName = "A".repeat(31);
        Tag tag = new Tag(longName);
        assertFalse(service.createTag(tag));
    }

    @Test
    void testCreateTagDuplicateName() {
        service.createTag(new Tag("Java"));
        
        // Tentativo di creare un tag con lo stesso nome
        Tag duplicateTag = new Tag("Java");
        assertFalse(service.createTag(duplicateTag));
    }

    @Test
    void testCreateTagDuplicateNameCaseInsensitive() {
        service.createTag(new Tag("Java"));
        
        // Tentativo di creare un tag con lo stesso nome
        Tag duplicateTag = new Tag("JAVA");
        assertFalse(service.createTag(duplicateTag));
    }

    @Test
    void testGetAllTags() {
        // Test con repository vuoto
        List<Tag> emptyTags = service.getAllTags();
        assertEquals(0, emptyTags.size());
        
        // Crea alcuni tag
        service.createTag(new Tag("Programmazione"));
        service.createTag(new Tag("Java"));
        service.createTag(new Tag("gwt"));
        
        List<Tag> allTags = service.getAllTags();
        assertEquals(3, allTags.size());
        
        // Verifica che i nomi siano presenti
        List<String> tagNames = allTags.stream()
                .map(Tag::getName)
                .toList();
        assertTrue(tagNames.contains("Programmazione"));
        assertTrue(tagNames.contains("Java"));
        assertTrue(tagNames.contains("gwt"));
    }

    @Test
    void testDeleteTagSuccess() {
        Tag tag = new Tag("daEliminare");
        service.createTag(tag);
        
        assertTrue(service.isTagNameExists("daEliminare"));
        assertTrue(service.deleteTag("daEliminare"));
        assertFalse(service.isTagNameExists("daEliminare"));
        
        List<Tag> allTags = service.getAllTags();
        assertEquals(0, allTags.size());
    }

    @Test
    void testIsTagNameExists() {
        assertFalse(service.isTagNameExists("NonEsistente"));
        assertFalse(service.isTagNameExists(null));
        assertFalse(service.isTagNameExists(""));
        
        service.createTag(new Tag("Esistente"));
        
        assertTrue(service.isTagNameExists("Esistente"));
        assertTrue(service.isTagNameExists("ESISTENTE")); 
    }

    @Test
    void testMultipleTagOperations() {
        // Crea diversi tag
        assertTrue(service.createTag(new Tag("Java")));
        assertTrue(service.createTag(new Tag("Python")));
        assertTrue(service.createTag(new Tag("JavaScript")));
        
        // Verifica che esistano
        assertEquals(3, service.getAllTags().size());
        assertTrue(service.isTagNameExists("Java"));
        assertTrue(service.isTagNameExists("Python"));
        assertTrue(service.isTagNameExists("JavaScript"));
        
        // Elimina uno
        assertTrue(service.deleteTag("Python"));
        assertEquals(2, service.getAllTags().size());
        assertFalse(service.isTagNameExists("Python"));
        
        // Gli altri devono essere ancora presenti
        assertTrue(service.isTagNameExists("Java"));
        assertTrue(service.isTagNameExists("JavaScript"));
    }
}
