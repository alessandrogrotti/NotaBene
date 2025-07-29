package com.google.gwt.sample.notabene.server;

import com.google.gwt.sample.notabene.shared.Tag;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

//Repository per gestire la persistenza dei tag con MapDB
public class TagRepository {
    
    private static final String TAGS_MAP_NAME = "tags";
    private static TagRepository instance;
    private DB tagDB;
    private ConcurrentMap<String, Tag> tagsMap;
    
    private TagRepository() {
        initializeDatabase();
    }
    
    public static synchronized TagRepository getInstance() {
        if (instance == null) {
            instance = new TagRepository();
        }
        return instance;
    }
    
    @SuppressWarnings("unchecked")
    private void initializeDatabase() {
        try {
            tagDB = DBMaker.fileDB(MapDBConstants.TAGDB_FILENAME)
                    .transactionEnable()
                    .closeOnJvmShutdown()
                    .make();
            
            System.out.println("Database TagDB creato/aperto con successo");
            
            tagsMap = tagDB.hashMap(TAGS_MAP_NAME)
                    .keySerializer(Serializer.STRING)
                    .valueSerializer(Serializer.JAVA)
                    .createOrOpen();
            
            System.out.println("TagsMap inizializzata con successo");            
            System.out.println("TagRepository inizializzato con " + tagsMap.size() + " tag");
        } catch (Exception e) {
            System.err.println("Errore nell'inizializzazione del database tag: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public boolean saveTag(Tag tag) {
        try {
            System.out.println("Tentativo di salvare tag: " + (tag != null ? tag.getName() : "null"));
            
            if (tagsMap == null) {
                System.err.println("TagsMap è null, impossibile salvare il tag");
                return false;
            }
            
            if (tagDB == null) {
                System.err.println("TagDB è null, impossibile salvare il tag");
                return false;
            }
            
            // nome come chiave 
            String key = tag.getName().toLowerCase().trim();
            System.out.println("Salvataggio con chiave: " + key);
            
            tagsMap.put(key, tag);
            tagDB.commit();
            
            System.out.println("Tag salvato con successo nel database");
            return true;
        } catch (Exception e) {
            if (tagDB != null) {
                tagDB.rollback();
            }
            System.err.println("Errore durante il salvataggio del tag: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public Tag findByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        return tagsMap.get(name.toLowerCase().trim());
    }
    
    public List<Tag> findAll() {
        if (tagsMap == null) {
            System.err.println("TagsMap è null, ritorno lista vuota");
            return new ArrayList<>();
        }
        return new ArrayList<>(tagsMap.values());
    }

    public boolean deleteTag(String name) {
        try {
            Tag tag = findByName(name);
            if (tag == null) {
                return false;
            }
            
            tagsMap.remove(tag.getName().toLowerCase().trim());
            tagDB.commit();
            return true;
        } catch (Exception e) {
            if (tagDB != null) {
                tagDB.rollback();
            }
            System.err.println("Errore durante l'eliminazione del tag: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean existsByName(String tagName) {
        if (tagName == null || tagName.trim().isEmpty()) {
            return false;
        }
        return tagsMap.containsKey(tagName.toLowerCase().trim());
    }
    
    public int count() {
        return tagsMap.size();
    }
    
    public void clear() {
        try {
            if (tagsMap != null) {
                tagsMap.clear();
                if (tagDB != null) {
                    tagDB.commit();
                }
                System.out.println("TagRepository pulito con successo");
            }
        } catch (Exception e) {
            if (tagDB != null) {
                tagDB.rollback();
            }
            System.err.println("Errore durante la pulizia del TagRepository: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
