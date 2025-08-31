package com.google.gwt.sample.notabene.shared;

import com.google.gwt.user.client.rpc.AsyncCallback;

//Interfaccia Asincrona per UserService utilizzata dal client GWT
public interface UserServiceAsync {
 
    void registerUser(User user, AsyncCallback<Boolean> callback);

    void isUsernameExists(String username, AsyncCallback<Boolean> callback);

    void authenticateUser(String username, String password, AsyncCallback<User> callback);
    
    void getAllUsers(AsyncCallback<java.util.List<User>> callback);
}
