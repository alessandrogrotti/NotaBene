package com.google.gwt.sample.notabene.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.sample.notabene.shared.User;
import com.google.gwt.sample.notabene.shared.Note;
import com.google.gwt.sample.notabene.shared.NoteService;
import com.google.gwt.sample.notabene.shared.NoteServiceAsync;
import java.util.List;


public class HomePage {
    private final NoteServiceAsync noteService = GWT.create(NoteService.class);

   private VerticalPanel panel = new VerticalPanel();
    private Label welcomeLabel = new Label("Benvenuto nella nostra applicazione!");
    private Button registerButton = new Button("Registrati");
    private Button loginButton = new Button("Accedi");
    private Button addNoteButton = new Button("Aggiungi nota");
    private Button manageTagsButton = new Button("Gestisci Tag");
    private Button logoutButton = new Button("Logout");
    private VerticalPanel notesPanel = new VerticalPanel(); //per dopo
    private VerticalPanel notesListPanel = new VerticalPanel(); //per dopo
    private String currentUsername; //teniamo traccia dell'utente corrente

    public HomePage() {
        panel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
        panel.setSpacing(20);
        panel.setWidth("100%");
       welcomeLabel.setStyleName("welcome-message");
        registerButton.setStyleName("home-button");
        loginButton.setStyleName("home-button");
        addNoteButton.setStyleName("home-button");
        manageTagsButton.setStyleName("home-button");
        logoutButton.setStyleName("back-button");
        
        panel.add(welcomeLabel);
        panel.add(registerButton);
        panel.add(loginButton);
    }

    public void show(boolean isAuthenticated, User user) {
        RootPanel.get("list").clear();
        panel.clear();
        panel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
        panel.setSpacing(20);
        panel.setWidth("100%");
        if (!isAuthenticated || user == null) {
            welcomeLabel.setText("Benvenuto nella nostra applicazione!");
            panel.add(welcomeLabel);
            panel.add(registerButton);
            panel.add(loginButton);
            RootPanel.get("list").add(panel);
        } else {
            welcomeLabel.setText("Benvenuto " + user.getName() + " " + user.getSurname() + "!");
            panel.add(welcomeLabel);
            
            notesPanel.clear();
            notesPanel.add(new Label("Qui ci saranno le note di: " + user.getName() + " " + user.getSurname() + "!"));

            panel.add(addNoteButton);
            panel.add(manageTagsButton);
            panel.add(logoutButton);
            panel.add(notesPanel);
            RootPanel.get("list").add(panel);
            
           // showNotesList(user.getName(), user.getSurname(), user.getUsername());
        }
    }


    public Button getRegisterButton() { return registerButton; }
    public Button getLoginButton() { return loginButton; }
    public Button getAddNoteButton() { return addNoteButton; }
    public Button getManageTagsButton() { return manageTagsButton; }
    public Button getLogoutButton() { return logoutButton; }
}
