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
            
        FlowPanel meta = new FlowPanel();
        meta.setStyleName("version-info");
        meta.add(dateLabel);
        meta.add(editorLabel);
        
        Label titleLabel = new Label("Titolo: " + currentNote.getTitle());
        titleLabel.setStyleName("version-title");
        
        HTML contentArea = new HTML();
        String content = currentNote.getContent();
        contentArea.setHTML("<div class='version-content'>Contenuto: " + content + "</div>");
            
        panel.add(currentLabel);
        panel.add(meta);
        panel.add(titleLabel);
        panel.add(contentArea);

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

        FlowPanel headerLeft = new FlowPanel();
        headerLeft.add(versionLabel);
        headerLeft.add(dateLabel);
        headerLeft.add(editorLabel);
        headerLeft.setStyleName("version-info");

        headerPanel.add(headerLeft);
        headerPanel.setCellWidth(headerLeft, "100%");

        Label titleLabel = new Label("Titolo: " + version.getTitle());
        titleLabel.setStyleName("version-title");

        HTML contentArea = new HTML();
        String content = version.getContent();
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
        DialogBox dialog = new DialogBox(true, true);
        dialog.setStyleName("version-dialog");
        dialog.setText("Dettagli versione v" + version.getVersionNumber());

        VerticalPanel body = new VerticalPanel();
        body.setStyleName("dialog-body");
        body.setWidth("100%");

        FlowPanel chips = new FlowPanel();
        chips.setStyleName("version-info");
        Label dateChip = new Label(dateFormat.format(version.getVersionCreatedAt()));
        dateChip.setStyleName("version-date");
        Label editorChip = new Label("da " + version.getEditorUsername());
        editorChip.setStyleName("version-editor");
        chips.add(dateChip);
        chips.add(editorChip);

        Label title = new Label("Titolo: " + version.getTitle());
        title.setStyleName("version-title");

        HTML content = new HTML("<div class='version-content'>" + version.getContent() + "</div>");

        body.add(chips);
        body.add(title);
        body.add(content);

        if (!version.getTags().isEmpty()) {
            FlowPanel tagsPanel = new FlowPanel();
            tagsPanel.setStyleName("version-tags");
            tagsPanel.add(new Label("Tag: "));
            for (String tag : version.getTags()) {
                Label tagLabel = new Label(tag);
                tagLabel.setStyleName("tag-label-small");
                tagsPanel.add(tagLabel);
            }
            body.add(tagsPanel);
        }

        HorizontalPanel actions = new HorizontalPanel();
        actions.setStyleName("dialog-actions");
        Button closeBtn = new Button("Chiudi");
        closeBtn.addClickHandler(e -> dialog.hide());
        actions.add(closeBtn);

        VerticalPanel container = new VerticalPanel();
        container.add(body);
        container.add(actions);

        dialog.setWidget(container);
        dialog.center();
        dialog.show();
    }
    
    public void show() {
        RootPanel.get("list").clear();
        RootPanel.get("list").add(panel);
    }
    
    public Button getBackButton() {
        return backButton;
    }
}