package com.google.gwt.sample.notabene.client;

import com.google.gwt.user.client.ui.*;
import com.google.gwt.sample.notabene.shared.Note;
import com.google.gwt.i18n.client.DateTimeFormat;
import java.util.Date;

public class NoteDetailPage {
    private VerticalPanel panel = new VerticalPanel();
    private Label titleLabel = new Label();
    private Label authorLabel = new Label();
    private Label createdDateLabel = new Label();
    private Label lastModifiedLabel = new Label();
    private HTML contentArea = new HTML();
    private FlowPanel tagsPanel = new FlowPanel();
    private Button backButton = new Button("Torna alla Lista");
    
    private static final DateTimeFormat dateFormat = DateTimeFormat.getFormat("dd/MM/yyyy HH:mm");
    
    public NoteDetailPage() {
        setupPage();
    }
    
    private void setupPage() {
        panel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
        panel.setSpacing(20);
        panel.setWidth("90%");
        
        // Applica stili coerenti con le altre pagine
        titleLabel.setStyleName("form-title");
        authorLabel.setStyleName("form-label");
        createdDateLabel.setStyleName("form-label");
        lastModifiedLabel.setStyleName("form-label");
        contentArea.setStyleName("note-detail-content");
        tagsPanel.setStyleName("note-detail-tags");
        backButton.setStyleName("back-button");
        
        // Struttura della pagina
        panel.add(backButton); // Pulsante in alto per facilità di navigazione
        panel.add(titleLabel);
        panel.add(authorLabel);
        panel.add(createdDateLabel);
        panel.add(lastModifiedLabel);
        
        // Sezione contenuto
        Label contentLabel = new Label("Contenuto:");
        contentLabel.setStyleName("form-label");
        panel.add(contentLabel);
        panel.add(contentArea);
        
        // Sezione tag
        Label tagsLabel = new Label("Tag:");
        tagsLabel.setStyleName("form-label");
        panel.add(tagsLabel);
        panel.add(tagsPanel);
    }
    
    public void showNote(Note note) {
        if (note == null) {
            titleLabel.setText("Nota non trovata");
            authorLabel.setText("");
            createdDateLabel.setText("");
            lastModifiedLabel.setText("");
            contentArea.setHTML("");
            tagsPanel.clear();
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
            lastModifiedLabel.setText("Ultima modifica: " + dateFormat.format(note.getLastModified()));
        } else {
            lastModifiedLabel.setText("Data ultima modifica non disponibile");
        }
        
        // mostriamo il contenuto 
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
}
