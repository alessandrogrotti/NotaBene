package com.google.gwt.sample.notabene.server;

import com.google.gwt.sample.notabene.shared.Tag;
import com.google.gwt.sample.notabene.shared.TagService;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import java.util.ArrayList;
import java.util.List;

//Implementazione servizio per gestione tag
public class TagServiceImpl extends RemoteServiceServlet implements TagService {
    
    private void initializeRepository() {
        try {
            TagRepository repository = TagRepository.getInstance();
            System.out.println("TagRepository inizializzato correttamente");
        } catch (Exception e) {
            System.err.println("Errore durante l'inizializzazione di TagRepository: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public boolean createTag(Tag tag) throws IllegalArgumentException {
        initializeRepository(); 
        
        try {
            if (tag == null) {
                System.err.println("Errore: Il tag non può essere null");
                return false;
            }
            
            if (tag.getName() == null || tag.getName().trim().isEmpty()) {
                System.err.println("Errore: Il nome del tag è obbligatorio");
                return false;
            }
            
            String tagName = tag.getName().trim();
            
            if (tagName.length() < 2) {
                System.err.println("Errore: Il nome del tag deve avere almeno 2 caratteri");
                return false;
            }
            
            if (tagName.length() > 30) {
                System.err.println("Errore: Il nome del tag non può superare i 30 caratteri");
                return false;
            }
            
            if (TagRepository.getInstance().existsByName(tagName)) {
                System.err.println("Errore: Un tag con questo nome esiste già: " + tagName);
                return false;
            }
            
            tag.setName(tagName);
            
            boolean result = TagRepository.getInstance().saveTag(tag);
            if (result) {
                System.out.println("Tag creato con successo: " + tag.getName());
            } else {
                System.err.println("Errore durante il salvataggio del tag nel repository");
            }
            return result;
            
        } catch (Exception e) {
            System.err.println("Errore imprevisto durante la creazione del tag: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public List<Tag> getAllTags() {
        initializeRepository(); 
        
        try {
            List<Tag> allTags = TagRepository.getInstance().findAll();
            System.out.println("Recuperati " + allTags.size() + " tag totali");
            return allTags;
        } catch (Exception e) {
            System.err.println("Errore durante il recupero di tutti i tag: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>(); 
        }
    }
    
    
    
    @Override
    public boolean deleteTag(String tagName) throws IllegalArgumentException {
        if (tagName == null || tagName.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome del tag obbligatorio");
        }
        
        try {
            boolean result = TagRepository.getInstance().deleteTag(tagName.trim());
            if (result) {
                System.out.println("Tag eliminato con successo, Nome: " + tagName);
            } else {
                throw new IllegalArgumentException("Nome non trovato");
            }
            return result;
        } catch (IllegalArgumentException e) {
            throw e; 
        } catch (Exception e) {
            System.err.println("Errore durante l'eliminazione del tag: " + e.getMessage());
            e.printStackTrace();
            throw new IllegalArgumentException("Errore durante l'eliminazione del tag");
        }
    }
    
    @Override
    public boolean isTagNameExists(String tagName) {
        if (tagName == null || tagName.trim().isEmpty()) {
            return false;
        }
        
        try {
            boolean exists = TagRepository.getInstance().existsByName(tagName.trim());
            System.out.println("Controllo esistenza tag '" + tagName + "': " + exists);
            return exists;
        } catch (Exception e) {
            System.err.println("Errore durante il controllo esistenza tag: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
