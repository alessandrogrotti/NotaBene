package com.google.gwt.sample.notabene.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.sample.notabene.shared.Note;
import com.google.gwt.sample.notabene.shared.NoteVersion;
import com.google.gwt.sample.notabene.shared.NoteVersionService;
import com.google.gwt.sample.notabene.shared.NoteVersionServiceAsync;
import java.util.List;

public class NoteVersionHistoryPage {
    
    private VerticalPanel panel = new VerticalPanel();
    private Label titleLabel = new Label("Cronologia Versioni");
    private Label noteInfoLabel = new Label();
    private VerticalPanel versionsPanel = new VerticalPanel();
    private Button backButton = new Button("Torna alla Nota");
    
    private Note currentNote;
    private static final DateTimeFormat dateFormat = DateTimeFormat.getFormat("dd/MM/yyyy HH:mm");
    
    private final NoteVersionServiceAsync versionService = GWT.create(NoteVersionService.class);
    
    public NoteVersionHistoryPage() {
        setupPage();
    }
    
    private void setupPage() {
        panel.setStyleName("main-panel");
        panel.setSpacing(20);
        panel.setWidth("100%");
        
        titleLabel.setStyleName("page-title");
        
        noteInfoLabel.setStyleName("note-info");
        
        versionsPanel.setStyleName("versions-panel");
        versionsPanel.setSpacing(10);
        versionsPanel.setWidth("100%");
        
        backButton.setStyleName("back-button");
        
        panel.add(titleLabel);
        panel.add(noteInfoLabel);
        panel.add(versionsPanel);
        panel.add(backButton);
    }
    
    public void showVersionHistory(Note note) {
        this.currentNote = note;
        
        if (note == null) {
            Window.alert("Errore: nota non trovata");
            return;
        }
        
        noteInfoLabel.setText("Nota: " + note.getTitle() + " (Proprietario: " + note.getOwnerUsername() + ")");
        
        loadVersions(note.getId());
    }
    
    private void loadVersions(String noteId) {
        versionsPanel.clear();
        versionsPanel.add(new Label("Caricamento versioni..."));
        
        versionService.getNoteVersions(noteId, new AsyncCallback<List<NoteVersion>>() {
            @Override
            public void onFailure(Throwable caught) {
                versionsPanel.clear();
                versionsPanel.add(new Label("Errore nel caricamento delle versioni: " + caught.getMessage()));
            }
            
            @Override
            public void onSuccess(List<NoteVersion> versions) {
                displayVersions(versions);
            }
        });
    }
    
    private void displayVersions(List<NoteVersion> versions) {
        versionsPanel.clear();
        
        if (versions.isEmpty()) {
            versionsPanel.add(new Label("Nessuna versione precedente disponibile."));
            return;
        }
        
        VerticalPanel currentVersionPanel = createCurrentVersionPanel();
        versionsPanel.add(currentVersionPanel);
        
        Label previousVersionsLabel = new Label("Versioni Precedenti:");
        previousVersionsLabel.setStyleName("section-label");
        versionsPanel.add(previousVersionsLabel);
        
        for (NoteVersion version : versions) {
            VerticalPanel versionPanel = createVersionPanel(version);
            versionsPanel.add(versionPanel);
        }
    }
    
    private VerticalPanel createCurrentVersionPanel() {
        VerticalPanel panel = new VerticalPanel();
        panel.setStyleName("current-version-panel");
        panel.setSpacing(5);
        panel.setWidth("100%");
        
        Label currentLabel = new Label("Versione Corrente (v" + currentNote.getVersionNumber() + ")");
        currentLabel.setStyleName("current-version-label");
        
        Label dateLabel = new Label("Ultima modifica: " + dateFormat.format(currentNote.getLastModified()));
        dateLabel.setStyleName("version-date");
        
        String editor = currentNote.getEditorUsername() != null ? 
                      currentNote.getEditorUsername() : currentNote.getOwnerUsername();
        Label editorLabel = new Label("Modificata da: " + editor);
        editorLabel.setStyleName("version-editor");
        
        Label titleLabel = new Label("Titolo: " + currentNote.getTitle());
        titleLabel.setStyleName("version-title");
        
        HTML contentArea = new HTML();
        String content = currentNote.getContent();
        if (content.length() > 100) {
            content = content.substring(0, 100) + "...";
        }
        contentArea.setHTML("<div class='version-content'>Contenuto: " + content + "</div>");
        
        if (!currentNote.getTags().isEmpty()) {
            FlowPanel tagsPanel = new FlowPanel();
            tagsPanel.setStyleName("version-tags");
            tagsPanel.add(new Label("Tag: "));
            
            for (String tag : currentNote.getTags()) {
                Label tagLabel = new Label(tag);
                tagLabel.setStyleName("tag-label-small");
                tagsPanel.add(tagLabel);
            }
            panel.add(tagsPanel);
        }
        
        panel.add(currentLabel);
        panel.add(dateLabel);
        panel.add(editorLabel);
        panel.add(titleLabel);
        panel.add(contentArea);
        
        return panel;
    }
    
    private VerticalPanel createVersionPanel(NoteVersion version) {
        VerticalPanel panel = new VerticalPanel();
        panel.setStyleName("version-panel");
        panel.setSpacing(5);
        panel.setWidth("100%");
        
        HorizontalPanel headerPanel = new HorizontalPanel();
        headerPanel.setWidth("100%");
        headerPanel.setStyleName("version-header");
        
        Label versionLabel = new Label("Versione " + version.getVersionNumber());
        versionLabel.setStyleName("version-number");
        
        Label dateLabel = new Label(dateFormat.format(version.getVersionCreatedAt()));
        dateLabel.setStyleName("version-date");
        
        Label editorLabel = new Label("da " + version.getEditorUsername());
        editorLabel.setStyleName("version-editor");
        
        Button viewButton = new Button("Visualizza");
        viewButton.setStyleName("version-view-button");
        viewButton.addClickHandler(event -> showVersionDetails(version));
        
        headerPanel.add(versionLabel);
        headerPanel.add(dateLabel);
        headerPanel.add(editorLabel);
        headerPanel.add(viewButton);
        
        Label titleLabel = new Label("Titolo: " + version.getTitle());
        titleLabel.setStyleName("version-title");
        
        HTML contentArea = new HTML();
        String content = version.getContent();
        if (content.length() > 100) {
            content = content.substring(0, 100) + "...";
        }
        contentArea.setHTML("<div class='version-content-preview'>Contenuto: " + content + "</div>");
        
        panel.add(headerPanel);
        panel.add(titleLabel);
        panel.add(contentArea);
        
        if (!version.getTags().isEmpty()) {
            FlowPanel tagsPanel = new FlowPanel();
            tagsPanel.setStyleName("version-tags");
            tagsPanel.add(new Label("Tag: "));
            
            for (String tag : version.getTags()) {
                Label tagLabel = new Label(tag);
                tagLabel.setStyleName("tag-label-small");
                tagsPanel.add(tagLabel);
            }
            panel.add(tagsPanel);
        }
        
        return panel;
    }
    
    private void showVersionDetails(NoteVersion version) {
        Note versionNote = version.toReadOnlyNote();
        
        String message = "Versione " + version.getVersionNumber() + "\n" +
                        "Data: " + dateFormat.format(version.getVersionCreatedAt()) + "\n" +
                        "Editor: " + version.getEditorUsername() + "\n\n" +
                        "Titolo: " + version.getTitle() + "\n\n" +
                        "Contenuto:\n" + version.getContent();
        
        Window.alert(message);
    }
    
    public void show() {
        RootPanel.get("list").clear();
        RootPanel.get("list").add(panel);
    }
    
    public Button getBackButton() {
        return backButton;
    }
}