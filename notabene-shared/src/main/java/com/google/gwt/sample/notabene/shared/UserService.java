package com.google.gwt.sample.notabene.shared;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

//Servizio per gestire le operazioni sugli utenti
@RemoteServiceRelativePath("userService")
public interface UserService extends RemoteService {
    
    /**
     * Registra un nuovo utente
     * @param user l'utente da registrare
     * @return true se la registrazione è avvenuta con successo, false altrimenti
     * @throws IllegalArgumentException se l'username è già esistente o i dati non sono validi
     */
    boolean registerUser(User user) throws IllegalArgumentException;
    
    /**
     * Controlla se un username è già in uso
     * @param username username da controllare
     * @return true se l'username è già in uso, false altrimenti
     */
    boolean isUsernameExists(String username);
}
