package com.google.gwt.sample.notabene.shared;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import java.util.List;

//Servizio per gestire le operazioni sugli utenti
@RemoteServiceRelativePath("userService")
public interface UserService extends RemoteService {
    
    boolean registerUser(User user) throws IllegalArgumentException;
    
    boolean isUsernameExists(String username);

    User authenticateUser(String username, String password);
    
   List<User> getAllUsers();
}
