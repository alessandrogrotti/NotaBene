package com.google.gwt.sample.notabene.shared;

import com.google.gwt.user.client.rpc.AsyncCallback;

//Interfaccia Asincrona per il servizio utente
public interface UserServiceAsync {
 
    void registerUser(User user, AsyncCallback<Boolean> callback);

    void isUsernameExists(String username, AsyncCallback<Boolean> callback);
}
