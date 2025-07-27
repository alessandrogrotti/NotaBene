package com.google.gwt.sample.notabene.server;

import com.google.gwt.sample.notabene.shared.User;
import com.google.gwt.sample.notabene.shared.UserService;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.password4j.Hash;
import com.password4j.Password;

// Implementazione del servizio UserService
public class UserServiceImpl extends RemoteServiceServlet implements UserService {
    
    private static final long serialVersionUID = 1L;
    private UserRepository userRepository;
    
    public UserServiceImpl() {
        userRepository = UserRepository.getInstance();
        //informazioni di debug all'avvio del servizio
        System.out.println("UserServiceImpl inizializzato");
        userRepository.printDatabaseInfo();
        userRepository.printAllUsers();
    }
    
    @Override
    public boolean registerUser(User user) throws IllegalArgumentException {
        // Validazione dei dati di input
        if (user == null) {
            throw new IllegalArgumentException("I dati dell'utente non possono essere nulli");
        }
        
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username è obbligatorio");
        }
        
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password è obbligatoria");
        }
        
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome è obbligatorio");
        }
        
        if (user.getSurname() == null || user.getSurname().trim().isEmpty()) {
            throw new IllegalArgumentException("Cognome è obbligatorio");
        }
        
        if (userRepository.userExists(user.getUsername())) {
            throw new IllegalArgumentException("Username già esistente: " + user.getUsername());
        }
        
        if (user.getPassword().length() < 4) {
            throw new IllegalArgumentException("Password deve essere di almeno 4 caratteri");
        }
        
        // Hashing della password con Password4j
        Hash hash = Password.hash(user.getPassword()).withBcrypt();
        String hashedPassword = hash.getResult();
        
        System.out.println("=== PASSWORD HASHING ===");
        System.out.println("Password originale: " + user.getPassword());
        System.out.println("Password hashata: " + hashedPassword);
        System.out.println("Lunghezza hash: " + hashedPassword.length());
        System.out.println("========================");
        
        user.setPassword(hashedPassword);
        
        // Salva l'utente
        try {
            boolean success = userRepository.saveUser(user);
            if (success) {
                System.out.println("Utente registrato con successo: " + user.getUsername());
            } else {
                System.err.println("Errore nel salvataggio dell'utente: " + user.getUsername());
            }
            return success;
        } catch (Exception e) {
            System.err.println("Errore durante la registrazione: " + e.getMessage());
            throw new IllegalArgumentException("Errore durante la registrazione dell'utente");
        }
    }

    @Override
    public User authenticateUser(String username, String password) {
        // Validazione dei parametri
        if (username == null || username.trim().isEmpty()) {
            return null;
        }
       
        if (password == null || password.trim().isEmpty()) {
            return null;
        }
       
        try {
            User storedUser = userRepository.getUser(username);
           
            if (storedUser != null) {
                System.out.println("=== PASSWORD VERIFICATION ===");
                System.out.println("Password inserita: " + password);
                System.out.println("Hash memorizzato: " + storedUser.getPassword());
               
                // Verifica la password usando Password4j
                boolean verified = Password.check(password, storedUser.getPassword()).withBcrypt();
               
                System.out.println("Verifica password: " + (verified ? "SUCCESSO" : "FALLIMENTO"));
                System.out.println("=============================");
               
                if (verified) {
                    System.out.println("Utente autenticato con successo: " + username);
                    // Crea una copia dell'utente senza la password per sicurezza
                    User safeUser = new User(
                        storedUser.getUsername(),
                        null, // Non inviamo la password al client
                        storedUser.getName(),
                        storedUser.getSurname()
                    );
                    return safeUser;
                } else {
                    System.out.println("Tentativo di autenticazione fallito per: " + username);
                    return null;
                }
            } else {
                System.out.println("Utente non trovato: " + username);
                return null;
            }
        } catch (Exception e) {
            System.err.println("Errore durante l'autenticazione: " + e.getMessage());
            return null;
        }
    }
    
    @Override
    public boolean isUsernameExists(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        
        try {
            return userRepository.userExists(username);
        } catch (Exception e) {
            System.err.println("Errore nel controllo dell'username: " + e.getMessage());
            return false;
        }
    }
}
