package com.google.gwt.sample.notabene.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.sample.notabene.shared.Note;
import com.google.gwt.sample.notabene.shared.NoteService;
import com.google.gwt.sample.notabene.shared.NoteServiceAsync;
import com.google.gwt.i18n.client.DateTimeFormat;
import java.util.Date;

public class NoteDetailPage {
    private final NoteServiceAsync noteService = GWT.create(NoteService.class);
    
    private VerticalPanel panel = new VerticalPanel();
    private Label titleLabel = new Label();
    private Label authorLabel = new Label();
    private Label createdDateLabel = new Label();
    private Label lastModifiedLabel = new Label();
    private HTML contentArea = new HTML();
    private FlowPanel tagsPanel = new FlowPanel();
    private Button backButton = new Button("Torna alla Lista");
    private Button deleteButton = new Button("Elimina");
    private Button editButton = new Button("Modifica");
    private Button duplicateButton = new Button("Duplica");
    private Button versionHistoryButton = new Button("Cronologia Versioni");
    private Button removeFromNoteButton = new Button("Rimuoviti da questa nota");
    private HorizontalPanel buttonPanel = new HorizontalPanel();
    private Label statusLabel = new Label();
    
    private Note currentNote;
    private String currentUsername;
    
    private static final DateTimeFormat dateFormat = DateTimeFormat.getFormat("dd/MM/yyyy HH:mm");
    
    public NoteDetailPage() {
        setupPage();
    }
    
    private void setupPage() {
        panel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
        panel.setSpacing(20);
        panel.setWidth("90%");
        panel.setStyleName("form-container");
        
        titleLabel.setStyleName("form-title");
        authorLabel.setStyleName("form-label");
        createdDateLabel.setStyleName("form-label");
        lastModifiedLabel.setStyleName("form-label");
        contentArea.setStyleName("note-detail-content");
        tagsPanel.setStyleName("note-detail-tags");
        backButton.setStyleName("back-button");
        versionHistoryButton.setStyleName("version-history-button");
        deleteButton.setStyleName("delete-button");
        editButton.setStyleName("edit-button");
        duplicateButton.setStyleName("duplicate-button");
        removeFromNoteButton.setStyleName("back-button");
        statusLabel.setStyleName("form-label");
        
        buttonPanel.setSpacing(10);
        buttonPanel.add(backButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(editButton);
        buttonPanel.add(duplicateButton);
        buttonPanel.add(versionHistoryButton);
        buttonPanel.add(removeFromNoteButton);
        
        panel.add(buttonPanel);
        panel.add(statusLabel);
        panel.add(titleLabel);
        panel.add(authorLabel);
        panel.add(createdDateLabel);
        panel.add(lastModifiedLabel);
        
        // sezione contenuto
        Label contentLabel = new Label("Contenuto:");
        contentLabel.setStyleName("form-label");
        panel.add(contentLabel);
        panel.add(contentArea);
        
        // sezione tag
        Label tagsLabel = new Label("Tag:");
        tagsLabel.setStyleName("form-label");
        panel.add(tagsLabel);
        panel.add(tagsPanel);
        
        removeFromNoteButton.setVisible(false);
        statusLabel.setVisible(false);
        setupRemoveFromNoteHandler();
    }
    
    private void setupRemoveFromNoteHandler() {
        removeFromNoteButton.addClickHandler(event -> {
            if (currentNote != null && currentUsername != null) {
                if (com.google.gwt.user.client.Window.confirm("Sei sicuro di volerti rimuovere da questa nota? Non potrai più accedervi.")) {
                    removeUserFromNote();
                }
            }
        });
    }
    
    private void removeUserFromNote() {
        statusLabel.setText("Rimozione in corso...");
        statusLabel.setStyleName("form-label");
        statusLabel.setVisible(true);
        
        noteService.removeUserFromNote(currentNote.getId(), currentUsername, currentUsername, new AsyncCallback<Boolean>() {
            @Override
            public void onFailure(Throwable caught) {
                statusLabel.setText("Errore durante la rimozione: " + caught.getMessage());
                statusLabel.setStyleName("error-message");
                statusLabel.setVisible(true);
            }
            
            @Override
            public void onSuccess(Boolean result) {
                if (result) {
                    statusLabel.setText("Ti sei rimosso con successo da questa nota.");
                    statusLabel.setStyleName("success-message");
                    statusLabel.setVisible(true);
                    
                    removeFromNoteButton.setVisible(false);
                    editButton.setVisible(false);
                    duplicateButton.setVisible(false);
                    versionHistoryButton.setVisible(false);
                } else {
                    statusLabel.setText("Errore durante la rimozione dalla nota");
                    statusLabel.setStyleName("error-message");
                    statusLabel.setVisible(true);
                }
            }
        });
    }
    
    public void showNote(Note note, String username) {
        this.currentNote = note; 
        this.currentUsername = username; 
        
        if (note == null) {
            titleLabel.setText("Nota non trovata");
            authorLabel.setText("");
            createdDateLabel.setText("");
            lastModifiedLabel.setText("");
            contentArea.setHTML("");
            tagsPanel.clear();
            deleteButton.setVisible(false);
            editButton.setVisible(false);
            versionHistoryButton.setVisible(false);
            return;
        }
        
        titleLabel.setText(note.getTitle());
        authorLabel.setText("Autore: " + note.getOwnerUsername());
        
        if (note.getCreatedAt() != null) {
            createdDateLabel.setText("Creata il: " + dateFormat.format(note.getCreatedAt()));
        } else {
            createdDateLabel.setText("Data di creazione non disponibile");
        }
        
        if (note.getLastModified() != null) {
            String lastModifiedText = "Ultima modifica: " + dateFormat.format(note.getLastModified());
            if (note.getEditorUsername() != null && !note.getEditorUsername().equals(note.getOwnerUsername())) {
                lastModifiedText += " da " + note.getEditorUsername();
            }
            lastModifiedLabel.setText(lastModifiedText);
        } else {
            lastModifiedLabel.setText("Data ultima modifica non disponibile");
        }

        String versionInfo = " (v" + note.getVersionNumber() + ")";
        lastModifiedLabel.setText(lastModifiedLabel.getText() + versionInfo);
        
        boolean isOwner = currentUsername != null && currentUsername.equals(note.getOwnerUsername());
        boolean canWrite = currentUsername != null && note.canWrite(currentUsername);
        boolean canRead = currentUsername != null && note.canRead(currentUsername);
        boolean hasPermissions = currentUsername != null && (note.getReadOnlyUsers().contains(currentUsername) || note.getWriteUsers().contains(currentUsername));

        deleteButton.setVisible(isOwner);
        editButton.setVisible(canWrite);
        duplicateButton.setVisible(canRead); // pulsante duplica visibile solo se l'utente ha i permessi in lettura
        versionHistoryButton.setVisible(note.hasVersionHistory());
        
        removeFromNoteButton.setVisible(!isOwner && hasPermissions);
        
        statusLabel.setVisible(false);
        
        // mostra il contenuto 
        String content = note.getContent();
        if (content != null) {
            content = content.replace("\n", "<br>");
            contentArea.setHTML(content);
        } else {
            contentArea.setHTML("Nessun contenuto");
        }
        
        // tag
        tagsPanel.clear();
        if (note.getTags() != null && !note.getTags().isEmpty()) {
            for (String tag : note.getTags()) {
                Label tagLabel = new Label(tag);
                tagLabel.setStyleName("note-tag-label");
                tagsPanel.add(tagLabel);
            }
        } else {
            Label noTagsLabel = new Label("Nessun tag");
            noTagsLabel.setStyleName("note-no-tags");
            tagsPanel.add(noTagsLabel);
        }
    }
    
    public void show() {
        RootPanel.get("list").clear();
        RootPanel.get("list").add(panel);
    }

    public Button getBackButton() {
        return backButton;
    }
    
    public Button getDeleteButton() {
        return deleteButton;
    }

    public Button getEditButton() {
        return editButton;
    }
    
    public Note getCurrentNote() {
        return currentNote;
    }

    public Button getVersionHistoryButton() {
        return versionHistoryButton;
    }

    public Button getDuplicateButton() {
        return duplicateButton;
    }
    
    public Button getRemoveFromNoteButton() {
        return removeFromNoteButton;
    }
}
