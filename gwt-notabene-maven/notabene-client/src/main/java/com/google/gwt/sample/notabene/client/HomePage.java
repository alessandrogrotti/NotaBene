package com.google.gwt.sample.notabene.client;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.RootPanel;

public class HomePage {
    private VerticalPanel panel = new VerticalPanel();
    private Label welcomeLabel = new Label("Benvenuto nella nostra applicazione!");
    private Button registerButton = new Button("Registrati");
    private Button loginButton = new Button("Accedi");
    private Button addNoteButton = new Button("Aggiungi nota");
    private Button manageTagsButton = new Button("Gestisci Tag");
    private VerticalPanel notesPanel = new VerticalPanel();

    public HomePage() {
        panel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
        panel.setSpacing(20);
        panel.setWidth("100%");
        welcomeLabel.setStyleName("welcome-message");
        registerButton.setStyleName("home-button");
        loginButton.setStyleName("home-button");
        panel.add(welcomeLabel);
        panel.add(registerButton);
        panel.add(loginButton);
    }

    public void show(boolean isAuthenticated, String name, String surname) {
        RootPanel.get("list").clear();
        panel.clear();
        panel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
        panel.setSpacing(20);
        panel.setWidth("100%");
        if (!isAuthenticated) {
            welcomeLabel.setText("Benvenuto nella nostra applicazione!");
            panel.add(welcomeLabel);
            panel.add(registerButton);
            panel.add(loginButton);
            RootPanel.get("list").add(panel);
        } else {
            showNotesList(name, surname);
        }
    }

    // Metodo per mostrare direttamente l'elenco delle note
    public void showNotesList(String name, String surname) {
        notesPanel.clear();
        notesPanel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
        notesPanel.setSpacing(20);
        notesPanel.setWidth("100%");
        Label notesTitle = new Label("Elenco note di " + name + " " + surname);
        notesTitle.setStyleName("notes-title");
        notesPanel.add(notesTitle);
        // Qui puoi aggiungere la logica per recuperare e mostrare le note dell'utente
        // Esempio statico:
        notesPanel.add(new Label("- Nota 1"));
        notesPanel.add(new Label("- Nota 2"));
        notesPanel.add(addNoteButton);
        notesPanel.add(manageTagsButton);
        RootPanel.get("list").add(notesPanel);
    }

    public Button getRegisterButton() { return registerButton; }
    public Button getLoginButton() { return loginButton; }
    public Button getAddNoteButton() { return addNoteButton; }
    public Button getManageTagsButton() { return manageTagsButton; }
}
