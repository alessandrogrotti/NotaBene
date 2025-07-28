package com.google.gwt.sample.notabene.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.sample.notabene.shared.User;
import com.google.gwt.sample.notabene.shared.UserService;
import com.google.gwt.sample.notabene.shared.UserServiceAsync;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class NotaBene implements EntryPoint {

    
    private final UserServiceAsync userService = GWT.create(UserService.class);
    private User currentUser = null;
    private HomePage homePage;
    private RegistrationForm registrationForm;
    private LoginForm loginForm;

 
    
//ENTRY POINT HOME PAGE BASIC
   public void onModuleLoad() {
        homePage = new HomePage();
        registrationForm = new RegistrationForm();
        loginForm = new LoginForm();
        setupEventHandlers();
        showHomePage();
    }

      private void showRegistrationForm() {
        registrationForm.show();
    }

    private void showLoginForm() {
        loginForm.show();
    }
    
  
     private void showHomePage() {
        boolean isAuthenticated = currentUser != null;
        String name = isAuthenticated ? currentUser.getName() : "";
        String surname = isAuthenticated ? currentUser.getSurname() : "";
        homePage.show(isAuthenticated, name, surname);
    }
    
//Gestore registrazione
  private void handleRegistration() {
        String username = registrationForm.getUsernameBox().getText().trim();
        String password = registrationForm.getPasswordBox().getText();
        String name = registrationForm.getNameBox().getText().trim();
        String surname = registrationForm.getSurnameBox().getText().trim();
        if (username.isEmpty() || password.isEmpty() || name.isEmpty() || surname.isEmpty()) {
            Window.alert("Tutti i campi sono obbligatori!");
            return;
        }
        if (username.length() < 3) {
            Window.alert("Username deve essere di almeno 3 caratteri!");
            return;
        }
        if (password.length() < 4) {
            Window.alert("Password deve essere di almeno 4 caratteri!");
            return;
        }
        User newUser = new User(username, password, name, surname);
        Button confirmRegButton = registrationForm.getConfirmButton();
        confirmRegButton.setEnabled(false);
        confirmRegButton.setText("Registrazione in corso...");
        userService.registerUser(newUser, new AsyncCallback<Boolean>() {
            @Override
            public void onFailure(Throwable caught) {
                confirmRegButton.setEnabled(true);
                confirmRegButton.setText("Conferma Registrazione");
                Window.alert("Errore durante la registrazione: " + caught.getMessage());
            }
            @Override
            public void onSuccess(Boolean result) {
                confirmRegButton.setEnabled(true);
                confirmRegButton.setText("Conferma Registrazione");
                if (result) {
                    Window.alert("Registrazione completata con successo per: " + name + " " + surname);
                    clearRegistrationForm();
                    showHomePage();
                } else {
                    Window.alert("Errore durante la registrazione. Riprova più tardi.");
                }
            }
        });
    }
 
    private void handleLogin() {
        String username = loginForm.getUsernameBox().getText().trim();
        String password = loginForm.getPasswordBox().getText();
        if (username.isEmpty() || password.isEmpty()) {
            Window.alert("Username e password sono obbligatori!");
            return;
        }
        Button confirmLoginButton = loginForm.getConfirmButton();
        confirmLoginButton.setEnabled(false);
        confirmLoginButton.setText("Accesso in corso...");
        userService.authenticateUser(username, password, new AsyncCallback<User>() {
            @Override
            public void onFailure(Throwable caught) {
                confirmLoginButton.setEnabled(true);
                confirmLoginButton.setText("Accedi");
                Window.alert("Errore durante l'accesso: " + caught.getMessage());
            }
            @Override
            public void onSuccess(User result) {
                confirmLoginButton.setEnabled(true);
                confirmLoginButton.setText("Accedi");
                if (result != null) {
                    Window.alert("Accesso effettuato con successo!\n" +
                               "Benvenuto " + result.getName() + " " + result.getSurname() + "!");
                    clearLoginForm();
                    currentUser = result;
                    showHomePage();
                } else {
                    Window.alert("Username o password non corretti!");
                }
            }
        });
    }

    //Pulisce i campi del form di login
    private void clearLoginForm() {
        loginForm.getUsernameBox().setText("");
        loginForm.getPasswordBox().setText("");
    }
    
    private void clearRegistrationForm() {
        registrationForm.getUsernameBox().setText("");
        registrationForm.getPasswordBox().setText("");
        registrationForm.getNameBox().setText("");
        registrationForm.getSurnameBox().setText("");
    }
   
   private void setupEventHandlers() {
        homePage.getRegisterButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                showRegistrationForm();
            }
        });
        homePage.getLoginButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                showLoginForm();
            }
        });
        registrationForm.getConfirmButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                handleRegistration();
            }
        });
        registrationForm.getBackButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                showHomePage();
            }
        });
        loginForm.getConfirmButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                handleLogin();
            }
        });
        loginForm.getBackButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                showHomePage();
            }
        });
        
    }
}