package com.google.gwt.sample.notabene.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.sample.notabene.shared.User;
import com.google.gwt.sample.notabene.shared.UserService;
import com.google.gwt.sample.notabene.shared.UserServiceAsync;

public class NotaBene implements EntryPoint {

    // Servizio per la gestione degli utenti
    private final UserServiceAsync userService = GWT.create(UserService.class);

    // Pannelli vari
    private VerticalPanel homePanel = new VerticalPanel();
    private VerticalPanel registerPanel = new VerticalPanel();
    private VerticalPanel loginPanel = new VerticalPanel(); //gettiamo le basi per login
    
    // Elementi HomePage
    private Label welcomeLabel = new Label("Benvenuto nella nostra applicazione!");
    private Button registerButton = new Button("Registrati");
    private Button loginButton = new Button("Accedi");
    
    // Elementi del form di registrazione
    private Label registerTitle = new Label("Registrazione");
    private Label usernameRegLabel = new Label("Username:");
    private TextBox usernameRegBox = new TextBox();
    private Label passwordRegLabel = new Label("Password:");
    private PasswordTextBox passwordRegBox = new PasswordTextBox();
    private Label nameRegLabel = new Label("Nome:");
    private TextBox nameRegBox = new TextBox();
    private Label surnameRegLabel = new Label("Cognome:");
    private TextBox surnameRegBox = new TextBox();
    private Button confirmRegButton = new Button("Conferma Registrazione");
    private Button backFromRegButton = new Button("Torna alla Home");

    // Elementi del form di login
    private Label loginTitle = new Label("Accesso");
    private Label usernameLoginLabel = new Label("Username:");
    private TextBox usernameLoginBox = new TextBox();
    private Label passwordLoginLabel = new Label("Password:");
    private PasswordTextBox passwordLoginBox = new PasswordTextBox();
    private Button confirmLoginButton = new Button("Accedi");
    private Button backFromLoginButton = new Button("Torna alla Home");
   
    // Utente loggato
    private User currentUser = null;
 
    // Pulsanti x pagina dopo login
    private Button viewNotesButton = new Button("Vedi elenco note");
    private Button addNoteButton = new Button("Aggiungi nota");
    
//ENTRY POINT HOME PAGE BASIC
    public void onModuleLoad() {
        setupHomePage();
        setupRegistrationForm();
        setupLoginForm();
        showHomePage();
    }
    
    //Set up homepage con due pulsanti per registrazione e futuro 
    private void setupHomePage() {
        // Stile e configurazione del pannello principale
        homePanel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
        homePanel.setSpacing(20);
        homePanel.setWidth("100%");
        
        welcomeLabel.setStyleName("welcome-message");
        
      
        registerButton.setStyleName("home-button");
        loginButton.setStyleName("home-button");

        homePanel.add(welcomeLabel);
        homePanel.add(registerButton);
        homePanel.add(loginButton);
        
        //event handlers (placeholder per future implementazioni)
        setupEventHandlers();
    }
    
    private void setupRegistrationForm() {
        // Configurazione pannello 
        registerPanel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
        registerPanel.setSpacing(15);
        registerPanel.setWidth("100%");

        // Stili
        registerTitle.setStyleName("form-title");
        usernameRegLabel.setStyleName("form-label");
        passwordRegLabel.setStyleName("form-label");
        nameRegLabel.setStyleName("form-label");
        surnameRegLabel.setStyleName("form-label");
        
        usernameRegBox.setStyleName("form-input");
        passwordRegBox.setStyleName("form-input");
        nameRegBox.setStyleName("form-input");
        surnameRegBox.setStyleName("form-input");
        
        confirmRegButton.setStyleName("form-button");
        backFromRegButton.setStyleName("back-button");
    
        registerPanel.add(registerTitle);
        registerPanel.add(usernameRegLabel);
        registerPanel.add(usernameRegBox);
        registerPanel.add(passwordRegLabel);
        registerPanel.add(passwordRegBox);
        registerPanel.add(nameRegLabel);
        registerPanel.add(nameRegBox);
        registerPanel.add(surnameRegLabel);
        registerPanel.add(surnameRegBox);
        registerPanel.add(confirmRegButton);
        registerPanel.add(backFromRegButton);
        
        // Event handlers
        confirmRegButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                handleRegistration();
            }
        });
        
        backFromRegButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                showHomePage();
            }
        });
    }

    private void setupLoginForm() {
        // Configurazione pannello
        loginPanel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
        loginPanel.setSpacing(15);
        loginPanel.setWidth("100%");

        // Stili
        loginTitle.setStyleName("form-title");
        usernameLoginLabel.setStyleName("form-label");
        passwordLoginLabel.setStyleName("form-label");
        
        usernameLoginBox.setStyleName("form-input");
        passwordLoginBox.setStyleName("form-input");
        
        confirmLoginButton.setStyleName("form-button");
        backFromLoginButton.setStyleName("back-button");
        
        loginPanel.add(loginTitle);
        loginPanel.add(usernameLoginLabel);
        loginPanel.add(usernameLoginBox);
        loginPanel.add(passwordLoginLabel);
        loginPanel.add(passwordLoginBox);
        loginPanel.add(confirmLoginButton);
        loginPanel.add(backFromLoginButton);
        
        // Event handlers
        confirmLoginButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                handleLogin();
            }
        });
       
        backFromLoginButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                showHomePage();
            }
        });
    }
  
    private void showHomePage() {
        RootPanel.get("list").clear();
        homePanel.clear();
        homePanel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
        homePanel.setSpacing(20);
        homePanel.setWidth("100%");

         if (currentUser == null) {
            // Utente non autenticato: mostra pulsanti di accesso e registrazione
            welcomeLabel.setText("Benvenuto nella nostra applicazione!");
            homePanel.add(welcomeLabel);
            homePanel.add(registerButton);
            homePanel.add(loginButton);
        } else {
            // Utente autenticato: mostra pulsanti note
            welcomeLabel.setText("Benvenuto, " + currentUser.getName() + " " + currentUser.getSurname() + "!");
            homePanel.add(welcomeLabel);
            homePanel.add(viewNotesButton);
            homePanel.add(addNoteButton);
        }
        RootPanel.get("list").add(homePanel);
    }
    
    private void showRegistrationForm() {
        RootPanel.get("list").clear();
        RootPanel.get("list").add(registerPanel);
    }
    
//Gestore registrazione
    private void handleRegistration() {
        String username = usernameRegBox.getText().trim();
        String password = passwordRegBox.getText();
        String name = nameRegBox.getText().trim();
        String surname = surnameRegBox.getText().trim();
        
        // Validazione base
        if (username.isEmpty() || password.isEmpty() || name.isEmpty() || surname.isEmpty()) {
            Window.alert("Tutti i campi sono obbligatori!");
            return;
        }
        
        if (password.length() < 4) {
            Window.alert("Password deve essere di almeno 4 caratteri!");
            return;
        }
        
        User newUser = new User(username, password, name, surname);
        
        confirmRegButton.setEnabled(false);
        confirmRegButton.setText("Registrazione in corso...");
        
        //servizio per registrare l'utente
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

    // Mostra il form di login
    private void showLoginForm() {
        RootPanel.get("list").clear();
        RootPanel.get("list").add(loginPanel);
    }
 
    //Gestisce il login dell'utente
    private void handleLogin() {
        String username = usernameLoginBox.getText().trim();
        String password = passwordLoginBox.getText();
       
        // Validazione base
        if (username.isEmpty() || password.isEmpty()) {
            Window.alert("Username e password sono obbligatori!");
            return;
        }
       
        // Disabilita il pulsante per evitare doppi invii
        confirmLoginButton.setEnabled(false);
        confirmLoginButton.setText("Accesso in corso...");
       
        // Chiama il servizio per autenticare l'utente
        userService.authenticateUser(username, password, new AsyncCallback<User>() {
            @Override
            public void onFailure(Throwable caught) {
                // Riabilita il pulsante
                confirmLoginButton.setEnabled(true);
                confirmLoginButton.setText("Accedi");
               
                // Mostra l'errore
                Window.alert("Errore durante l'accesso: " + caught.getMessage());
            }
           
            @Override
            public void onSuccess(User result) {
                // Riabilita il pulsante
                confirmLoginButton.setEnabled(true);
                confirmLoginButton.setText("Accedi");
               
                if (result != null) {
                    Window.alert("Accesso effettuato con successo!\n" +
                               "Benvenuto " + result.getName() + " " + result.getSurname() + "!");
                    // Pulisci i campi e mostra la home post-login
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
        usernameLoginBox.setText("");
        passwordLoginBox.setText("");
    }
    
    private void clearRegistrationForm() {
        usernameRegBox.setText("");
        passwordRegBox.setText("");
        nameRegBox.setText("");
        surnameRegBox.setText("");
    }
   
    private void setupEventHandlers() {
        registerButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                showRegistrationForm();
            }
        });
        
        loginButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                showLoginForm();
            }
        });
    }
}