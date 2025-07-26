package com.google.gwt.sample.notabene.server;

import com.google.gwt.sample.notabene.shared.User;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import java.util.concurrent.ConcurrentMap;

// Repository per gestire la persistenza degli utenti con MapDB
public class UserRepository {
    
    private static final String USERS_MAP_NAME = "users";
    private static UserRepository instance;
    private DB db;
    private ConcurrentMap<String, User> usersMap;
    
    private UserRepository() {
        initializeDatabase();
    }
    
    public static synchronized UserRepository getInstance() {
        if (instance == null) {
            instance = new UserRepository();
        }
        return instance;
    }
    
    @SuppressWarnings("unchecked")
    private void initializeDatabase() {
        try {
            // Crea o apre il database MapDB
            db = DBMaker
                .fileDB(MapDBConstants.DB_FILENAME)
                .transactionEnable()
                .closeOnJvmShutdown()
                .make();
            
            // Crea o recupera la mappa degli utenti
            usersMap = (ConcurrentMap<String, User>) db
                .hashMap(USERS_MAP_NAME)
                .keySerializer(Serializer.STRING)
                .valueSerializer(Serializer.JAVA)
                .createOrOpen();
                
        } catch (Exception e) {
            System.err.println("Errore nell'inizializzazione del database: " + e.getMessage());
            e.printStackTrace();
        }
    }
    

    public boolean saveUser(User user) {
        if (user == null || user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            return false;
        }
        
        try {
            usersMap.put(user.getUsername(), user);
            db.commit();
            return true;
        } catch (Exception e) {
            System.err.println("Errore nel salvataggio dell'utente: " + e.getMessage());
            db.rollback();
            return false;
        }
    }
    

    public User getUser(String username) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }
        
        try {
            return usersMap.get(username);
        } catch (Exception e) {
            System.err.println("Errore nel recupero dell'utente: " + e.getMessage());
            return null;
        }
    }
    

    public boolean userExists(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        
        try {
            return usersMap.containsKey(username);
        } catch (Exception e) {
            System.err.println("Errore nel controllo esistenza utente: " + e.getMessage());
            return false;
        }
    }

    public void close() {
        if (db != null && !db.isClosed()) {
            try {
                db.close();
            } catch (Exception e) {
                System.err.println("Errore nella chiusura del database: " + e.getMessage());
            }
        }
    }
    

    public int getUserCount() {
        try {
            return usersMap.size();
        } catch (Exception e) {
            System.err.println("Errore nel conteggio degli utenti: " + e.getMessage());
            return 0;
        }
    }
    
   //Metodo per verificare lo stato del database
    public boolean isDatabaseOperational() {
        try {
            // Test semplice: prova a fare una query sulla mappa
            usersMap.size();
            return !db.isClosed();
        } catch (Exception e) {
            System.err.println("Database non operativo: " + e.getMessage());
            return false;
        }
    }
    
    public void printDatabaseInfo() {
        try {
            System.out.println("=== Database Info ===");
            System.out.println("Database chiuso: " + db.isClosed());
            System.out.println("Numero utenti: " + usersMap.size());
            System.out.println("Database operativo: " + isDatabaseOperational());
            System.out.println("====================");
        } catch (Exception e) {
            System.err.println("Errore nella stampa info database: " + e.getMessage());
        }
    }
    
    public void printAllUsers() {
        try {
            System.out.println("=== Utenti registrati ===");
            for (User user : usersMap.values()) {
                System.out.println(user);
            }
            System.out.println("=========================");
        } catch (Exception e) {
            System.err.println("Errore nella stampa degli utenti: " + e.getMessage());
        }
    }
}
