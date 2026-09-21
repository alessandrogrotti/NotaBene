package com.google.gwt.sample.notabene.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.Window;
import com.google.gwt.sample.notabene.shared.User;
import com.google.gwt.sample.notabene.shared.Note;
import com.google.gwt.sample.notabene.shared.NoteService;
import com.google.gwt.sample.notabene.shared.NoteServiceAsync;
import com.google.gwt.sample.notabene.shared.NoteFilter;
import com.google.gwt.sample.notabene.client.utils.NoteFilterUtils;
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
    private VerticalPanel notesPanel = new VerticalPanel();
    private VerticalPanel notesListPanel = new VerticalPanel();
    private NoteSearchPanel searchPanel;
    private List<Note> allNotes;
    private String currentUsername;

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
        
        searchPanel = new NoteSearchPanel();
        setupSearchHandler();
        
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
            VerticalPanel loginPanel = new VerticalPanel();
            loginPanel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
            loginPanel.setSpacing(20);
            loginPanel.setWidth("100%");
            loginPanel.setStyleName("form-container");
            
            welcomeLabel.setText("Benvenuto nella nostra applicazione!");
            welcomeLabel.setStyleName("form-title");
            
            loginPanel.add(welcomeLabel);
            loginPanel.add(registerButton);
            loginPanel.add(loginButton);
            
            RootPanel.get("list").clear();
            RootPanel.get("list").add(loginPanel);
        } else {
            currentUsername = user.getUsername();
            welcomeLabel.setText("Benvenuto " + user.getName() + " " + user.getSurname() + "!");
            panel.add(welcomeLabel);

            panel.add(addNoteButton);
            panel.add(manageTagsButton);
            panel.add(logoutButton);

            panel.add(searchPanel);

            loadUserNotes(user.getUsername());

            panel.add(notesPanel);
            RootPanel.get("list").add(panel);
        }
    }

    private void loadUserNotes(String username) {
        notesPanel.clear();
        notesPanel.add(new Label("Caricamento note..."));
        //caroca note user
        noteService.getUserNotes(username, new AsyncCallback<List<Note>>() {
            @Override
            public void onFailure(Throwable caught) {
                notesPanel.clear();
                notesPanel.add(new Label("Errore nel caricamento delle note: " + caught.getMessage()));
            }
            
            @Override
            public void onSuccess(List<Note> userNotes) {
                // carica note accessibili
                noteService.getAccessibleNotes(username, new AsyncCallback<List<Note>>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        Window.alert("Errore nel caricamento delle note accessibili: " + caught.getMessage());
                                   }
                    
                    @Override
                    public void onSuccess(List<Note> accessibleNotes) {
                        for (Note accessibleNote : accessibleNotes) {
                            boolean alreadyExists = false;
                            for (Note userNote : userNotes) {
                                if (userNote.getId().equals(accessibleNote.getId())) {
                                    alreadyExists = true;
                                    break;
                                }
                            }
                            if (!alreadyExists) {
                                userNotes.add(accessibleNote);
                            }
                        }
                        allNotes = userNotes;
                        displayNotes(userNotes);
                    }
                });
            }
        });
    }
    
    private void displayNotes(List<Note> notes) {
        notesPanel.clear();
        
        if (notes == null || notes.isEmpty()) {
            Label noNotesLabel = new Label("Nessuna nota disponibile.");
            noNotesLabel.setStyleName("form-label");
            notesPanel.add(noNotesLabel);
            return;
        }
        
        Label notesTitle = new Label("Le tue note (" + notes.size() + "):");
        notesTitle.setStyleName("form-title");
        notesPanel.add(notesTitle);
        
        // container per le note
        VerticalPanel notesContainer = new VerticalPanel();
        notesContainer.setSpacing(10);
        notesContainer.setWidth("100%");
        
        for (Note note : notes) {
            createNoteItem(note, notesContainer);
        }
        
        notesPanel.add(notesContainer);
    }
    
    private void createNoteItem(Note note, VerticalPanel container) {
        VerticalPanel noteItem = new VerticalPanel();
    noteItem.setStyleName("note-item");
    noteItem.addStyleName("clickable-item");
    noteItem.setWidth("100%"); 
    noteItem.setSpacing(8);
        
        Label titleLabel = new Label(note.getTitle());
        titleLabel.setStyleName("note-item-title");
        
        String preview = note.getContent();
        if (preview != null) {
            if (preview.length() > 150) {
                preview = preview.substring(0, 150) + "...";
            }
        } else {
            preview = "Nessun contenuto";
        }
        Label previewLabel = new Label(preview);
        previewLabel.setStyleName("note-item-preview");
        
        String authorInfo = "Autore: " + note.getOwnerUsername();
        if (note.getCreatedAt() != null) {
            authorInfo += " • " + com.google.gwt.i18n.client.DateTimeFormat.getFormat("dd/MM/yyyy HH:mm").format(note.getCreatedAt());
        }
        Label authorLabel = new Label(authorInfo);
        authorLabel.setStyleName("note-item-author");
        
        noteItem.add(titleLabel);
        noteItem.add(previewLabel);
        noteItem.add(authorLabel);
        
        if (note.getTags() != null && !note.getTags().isEmpty()) {
            HorizontalPanel tagsPanel = new HorizontalPanel();
            tagsPanel.setSpacing(5);
            Label tagsPrefix = new Label("Tag: ");
            tagsPrefix.setStyleName("note-item-tags-prefix");
            tagsPanel.add(tagsPrefix);
            
            for (String tag : note.getTags()) {
                Label tagLabel = new Label(tag);
                tagLabel.setStyleName("note-item-tag");
                tagsPanel.add(tagLabel);
            }
            noteItem.add(tagsPanel);
        }
        
        titleLabel.addDomHandler(event -> {
            if (onNoteClickHandler != null) {
                onNoteClickHandler.onNoteClick(note);
            }
        }, com.google.gwt.event.dom.client.ClickEvent.getType());
        
        previewLabel.addDomHandler(event -> {
            if (onNoteClickHandler != null) {
                onNoteClickHandler.onNoteClick(note);
            }
        }, com.google.gwt.event.dom.client.ClickEvent.getType());
        
        container.add(noteItem);
    }
    
    public interface NoteClickHandler {
        void onNoteClick(Note note);
    }
    private NoteClickHandler onNoteClickHandler;
    
    public void setNoteClickHandler(NoteClickHandler handler) {
        this.onNoteClickHandler = handler;
    }
    
    private void setupSearchHandler() {
        if (searchPanel != null) {
            searchPanel.setSearchHandler(new NoteSearchPanel.SearchHandler() {
                @Override
                public void onSearch(NoteFilter filter) {
                    performSearch(filter);
                }
            });
        }
    }

    private void performSearch(NoteFilter filter) {
        if (allNotes == null) {
            return;
        }
        List<Note> filteredNotes;
        if (filter.isEmpty()) {
            filteredNotes = allNotes;
        } else {
            filteredNotes = NoteFilterUtils.filterNotes(allNotes, filter);
            filteredNotes = NoteFilterUtils.sortByRelevance(filteredNotes, filter);
        }
        displayNotes(filteredNotes);
        if (filteredNotes.isEmpty() && !filter.isEmpty()) {
            showNoResultsMessage();
        }
    }

    private void showNoResultsMessage() {
        notesPanel.clear();
        
        VerticalPanel noResultsPanel = new VerticalPanel();
        noResultsPanel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
        noResultsPanel.setSpacing(15);
        noResultsPanel.setStyleName("no-results-panel");
        
        Label noResultsLabel = new Label("Nessuna nota trovata");
        noResultsLabel.setStyleName("no-results-title");
        
        Label suggestionLabel = new Label("Prova a modificare i criteri di ricerca");
        suggestionLabel.setStyleName("no-results-text");
        
        noResultsPanel.add(noResultsLabel);
        noResultsPanel.add(suggestionLabel);
        
        notesPanel.add(noResultsPanel);
    }
    

    public Button getRegisterButton() { return registerButton; }
    public Button getLoginButton() { return loginButton; }
    public Button getAddNoteButton() { return addNoteButton; }
    public Button getManageTagsButton() { return manageTagsButton; }
    public Button getLogoutButton() { return logoutButton; }
}
