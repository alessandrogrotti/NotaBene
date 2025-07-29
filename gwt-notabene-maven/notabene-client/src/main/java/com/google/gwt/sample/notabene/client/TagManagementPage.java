package com.google.gwt.sample.notabene.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.sample.notabene.shared.Tag;
import com.google.gwt.sample.notabene.shared.TagService;
import com.google.gwt.sample.notabene.shared.TagServiceAsync;

import java.util.List;

/**
 * Pagina per gestire i tag del sistema
 */
public class TagManagementPage {
    
    private final TagServiceAsync tagService = GWT.create(TagService.class);
    
    private VerticalPanel mainPanel = new VerticalPanel();
    private Label titleLabel = new Label("Gestione Tag");
    private Button backButton = new Button("Torna alla Home");
    private Button addTagButton = new Button("Aggiungi Tag");
    
    // Form per aggiungere un nuovo tag
    private VerticalPanel addTagForm = new VerticalPanel();
    private TextBox tagNameBox = new TextBox();
    private Button saveTagButton = new Button("Salva Tag");
    private Button cancelButton = new Button("Annulla");
    private Label errorLabel = new Label();
    private Label successLabel = new Label();
    
    // Pannello per la lista dei tag
    private VerticalPanel tagListPanel = new VerticalPanel();
    private Label tagListTitle = new Label("Tutti i Tag:");
    
    private boolean isFormVisible = false;
    
    public TagManagementPage() {
        setupUI();
        setupEventHandlers();
    }
    
    private void setupUI() {
        mainPanel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
        mainPanel.setSpacing(20);
        mainPanel.setWidth("100%");
        
        // Titolo
        titleLabel.setStyleName("form-title");
        mainPanel.add(titleLabel);
        
        // Pulsanti di navigazione
        addTagButton.setStyleName("form-button");
        backButton.setStyleName("back-button");
        mainPanel.add(addTagButton);
        mainPanel.add(backButton);
        
        // Setup del form per aggiungere tag
        setupAddTagForm();
        
        // Setup della lista tag
        setupTagList();
        
        // Labels per messaggi
        errorLabel.setStyleName("error-message");
        successLabel.setStyleName("success-message");
        mainPanel.add(errorLabel);
        mainPanel.add(successLabel);
        
        hideMessages();
        hideAddTagForm();
    }
    
    private void setupAddTagForm() {
        addTagForm.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
        addTagForm.setSpacing(15);
        addTagForm.setWidth("100%");
        addTagForm.setStyleName("tag-form");
        
        Label formTitle = new Label("Aggiungi Nuovo Tag");
        formTitle.setStyleName("form-title");
        addTagForm.add(formTitle);
        
        // Nome tag
        Label nameLabel = new Label("Nome Tag:");
        nameLabel.setStyleName("form-label");
        tagNameBox.setStyleName("form-input");
        tagNameBox.getElement().setPropertyString("placeholder", "Inserisci il nome del tag");
        addTagForm.add(nameLabel);
        addTagForm.add(tagNameBox);
        
        // Pulsanti del form
        saveTagButton.setStyleName("form-button");
        cancelButton.setStyleName("back-button");
        addTagForm.add(saveTagButton);
        addTagForm.add(cancelButton);
        
        mainPanel.add(addTagForm);
    }
    
    private void setupTagList() {
        tagListTitle.setStyleName("form-title");
        tagListPanel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
        tagListPanel.setSpacing(15);
        tagListPanel.setWidth("100%");
        tagListPanel.add(tagListTitle);
        
        mainPanel.add(tagListPanel);
    }
    
    private void setupEventHandlers() {
        addTagButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (isFormVisible) {
                    hideAddTagForm();
                } else {
                    showAddTagForm();
                }
            }
        });
        
        saveTagButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                saveTag();
            }
        });
        
        cancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                hideAddTagForm();
                clearForm();
            }
        });
    }
    
    public void show() {
        RootPanel.get("list").clear();
        RootPanel.get("list").add(mainPanel);
        
        // Carica tutti i tag
        loadAllTags();
    }
    
    private void showAddTagForm() {
        addTagForm.setVisible(true);
        isFormVisible = true;
        addTagButton.setText("Nascondi Form");
        hideMessages();
    }
    
    private void hideAddTagForm() {
        addTagForm.setVisible(false);
        isFormVisible = false;
        addTagButton.setText("Aggiungi Tag");
        hideMessages();
    }
    
    private void saveTag() {
        hideMessages();
        
        String tagName = tagNameBox.getText().trim();
        
        // Validazione client-side
        if (tagName.isEmpty()) {
            showError("Il nome del tag è obbligatorio");
            return;
        }
        
        if (tagName.length() < 2) {
            showError("Il nome del tag deve avere almeno 2 caratteri");
            return;
        }
        
        if (tagName.length() > 30) {
            showError("Il nome del tag non può superare i 30 caratteri");
            return;
        }
        
        // Crea il tag
        Tag newTag = new Tag(tagName);
        
        // Salva il tag tramite servizio
        tagService.createTag(newTag, new AsyncCallback<Boolean>() {
            @Override
            public void onFailure(Throwable caught) {
                showError("Errore durante la creazione del tag: " + caught.getMessage());
            }
            
            @Override
            public void onSuccess(Boolean result) {
                if (result) {
                    showSuccess("Tag creato con successo!");
                    clearForm();
                    hideAddTagForm();
                    loadAllTags(); // Ricarica la lista
                } else {
                    showError("Errore durante la creazione del tag");
                }
            }
        });
    }
    
    private void loadAllTags() {
        tagService.getAllTags(new AsyncCallback<List<Tag>>() {
            @Override
            public void onFailure(Throwable caught) {
                showError("Errore durante il caricamento dei tag: " + caught.getMessage());
            }
            
            @Override
            public void onSuccess(List<Tag> tags) {
                displayTags(tags);
            }
        });
    }
    
    private void displayTags(List<Tag> tags) {
        // Pulisce la lista precedente (mantiene solo il titolo)
        tagListPanel.clear();
        tagListPanel.add(tagListTitle);
        
        if (tags.isEmpty()) {
            Label noTagsLabel = new Label("Non ci sono ancora tag nel sistema.");
            noTagsLabel.setStyleName("form-label");
            tagListPanel.add(noTagsLabel);
        } else {
            for (Tag tag : tags) {
                HorizontalPanel tagPanel = createTagPanel(tag);
                tagListPanel.add(tagPanel);
            }
        }
    }
    
    private HorizontalPanel createTagPanel(Tag tag) {
        HorizontalPanel tagPanel = new HorizontalPanel();
        tagPanel.setSpacing(15);
        tagPanel.setWidth("400px");
        tagPanel.setStyleName("tag-item");
        tagPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
        
        // Nome del tag
        Label nameLabel = new Label(tag.getName());
        nameLabel.setStyleName("form-label");
        nameLabel.setWidth("200px");
        
        // Pulsante elimina
        Button deleteButton = new Button("Elimina");
        deleteButton.setStyleName("delete-button");
        deleteButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (com.google.gwt.user.client.Window.confirm("Sei sicuro di voler eliminare il tag '" + tag.getName() + "'?")) {
                    deleteTag(tag.getName());
                }
            }
        });
        
        tagPanel.add(nameLabel);
        tagPanel.add(deleteButton);
        
        return tagPanel;
    }
    
    private void deleteTag(String tagName) {
        tagService.deleteTag(tagName, new AsyncCallback<Boolean>() {
            @Override
            public void onFailure(Throwable caught) {
                showError("Errore durante l'eliminazione del tag: " + caught.getMessage());
            }
            
            @Override
            public void onSuccess(Boolean result) {
                if (result) {
                    showSuccess("Tag eliminato con successo!");
                    loadAllTags(); // Ricarica la lista
                } else {
                    showError("Errore durante l'eliminazione del tag");
                }
            }
        });
    }
    
    private void clearForm() {
        tagNameBox.setText("");
    }
    
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        successLabel.setVisible(false);
    }
    
    private void showSuccess(String message) {
        successLabel.setText(message);
        successLabel.setVisible(true);
        errorLabel.setVisible(false);
    }
    
    private void hideMessages() {
        errorLabel.setVisible(false);
        successLabel.setVisible(false);
    }
    
    public Button getBackButton() {
        return backButton;
    }
}
