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
    
//ENTRY POINT HOME PAGE BASIC
    public void onModuleLoad() {
        setupHomePage();
        setupRegistrationForm();
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
    
  
    private void showHomePage() {
        RootPanel.get("list").clear();
        homePanel.clear();
        homePanel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
        homePanel.setSpacing(20);
        homePanel.setWidth("100%");
        
            welcomeLabel.setText("Benvenuto nella nostra applicazione!");
            homePanel.add(welcomeLabel);
            homePanel.add(registerButton);
            homePanel.add(loginButton);
        
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
                //todo
            }
        });
    }
}