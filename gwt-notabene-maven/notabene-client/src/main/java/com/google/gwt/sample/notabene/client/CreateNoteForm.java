package com.google.gwt.sample.notabene.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.sample.notabene.shared.NotePermission;
import com.google.gwt.sample.notabene.shared.Tag;
import com.google.gwt.sample.notabene.shared.TagService;
import com.google.gwt.sample.notabene.shared.TagServiceAsync;
import com.google.gwt.sample.notabene.shared.User;
import com.google.gwt.sample.notabene.shared.UserService;
import com.google.gwt.sample.notabene.shared.UserServiceAsync;

import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class CreateNoteForm {
    private final TagServiceAsync tagService = GWT.create(TagService.class);
    private final UserServiceAsync userService = GWT.create(UserService.class);
    
    private VerticalPanel panel = new VerticalPanel();
    private Label formTitle = new Label("Crea una nuova nota");
    private Label titleLabel = new Label("Titolo:");
    private TextBox titleBox = new TextBox();
    private Label titleCountLabel = new Label("0/50 caratteri");
    private Label contentLabel = new Label("Contenuto:");
    private TextArea contentArea = new TextArea();
    private Label contentCountLabel = new Label("0/280 caratteri");
    
    private Label tagsLabel = new Label("Tag:");
    private VerticalPanel tagsSection = new VerticalPanel();
    private Label availableTagsLabel = new Label("Tag disponibili:");
    private FlowPanel availableTagsFlow = new FlowPanel();
    private FlowPanel selectedTagsFlow = new FlowPanel();
    private Label selectedTagsLabel = new Label("Tag selezionati:");
    private Set<String> selectedTags = new HashSet<>();
    
    private Label permissionLabel = new Label("Permessi:");
    private ListBox permissionBox = new ListBox();

    private VerticalPanel usersSection = new VerticalPanel();
    private Label readUsersLabel = new Label("Utenti con accesso in lettura:");
    private FlowPanel availableUsersFlow = new FlowPanel();
    private FlowPanel selectedReadUsersFlow = new FlowPanel();
    private Set<String> selectedReadUsers = new HashSet<>();
    
    private Label writeUsersLabel = new Label("Utenti con accesso in scrittura:");
    private FlowPanel selectedWriteUsersFlow = new FlowPanel();
    private Set<String> selectedWriteUsers = new HashSet<>();
    
    private HorizontalPanel buttonPanel = new HorizontalPanel();
    private Button createButton = new Button("Crea Nota");
    private Button cancelButton = new Button("Annulla");
    
    private List<Tag> availableTags = new ArrayList<>();
    private List<User> availableUsers = new ArrayList<>();

    public CreateNoteForm() {
        setupForm();
        loadAvailableData();
        
        // Inizializza i conteggi caratteri
        updateTitleCount();
        updateContentCount();
    }

    private void setupForm() {
        panel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
        panel.setSpacing(15);
        panel.setWidth("80%");
        panel.setStyleName("form-container"); // container stilizzato
   
        formTitle.setStyleName("form-title");
        titleLabel.setStyleName("form-label");
        contentLabel.setStyleName("form-label");
        tagsLabel.setStyleName("form-label");
        permissionLabel.setStyleName("form-label");
        readUsersLabel.setStyleName("form-label");
        writeUsersLabel.setStyleName("form-label");
        
        titleBox.setStyleName("form-input");
        titleBox.setWidth("400px");
        
        // Controllo in tempo reale per il titolo con limite fisico
        titleBox.addKeyUpHandler(event -> {
            String text = titleBox.getText();
            if (text.length() > 50) {
                titleBox.setText(text.substring(0, 50));
            }
            updateTitleCount();
        });
        
        titleCountLabel.setStyleName("character-count");
        
        contentArea.setStyleName("form-input");
        contentArea.setSize("400px", "200px");
        contentArea.setCharacterWidth(50);
        contentArea.setVisibleLines(10);
        
        // Controllo in tempo reale per il contenuto con limite fisico 
        contentArea.addKeyUpHandler(event -> {
            String text = contentArea.getText();
            if (text.length() > 280) {
                contentArea.setText(text.substring(0, 280));
            }
            updateContentCount();
        });
        
        contentCountLabel.setStyleName("character-count");
        
        permissionBox.setStyleName("form-input");
        permissionBox.setWidth("400px");
        
        createButton.setStyleName("form-button");
        cancelButton.setStyleName("back-button");
        
        // listbox permessi
        permissionBox.addItem(NotePermission.PRIVATE.getDisplayName(), NotePermission.PRIVATE.name());
        permissionBox.addItem(NotePermission.READ_ONLY.getDisplayName(), NotePermission.READ_ONLY.name());
        permissionBox.addItem(NotePermission.READ_WRITE.getDisplayName(), NotePermission.READ_WRITE.name());
        
        // event handler per abilitare/disabilitare i campi utenti
        permissionBox.addChangeHandler(event -> {
            updateUserSectionVisibility();
        });
        
        setupTagsSection();
        setupUsersSection();
        
        buttonPanel.setSpacing(10);
        buttonPanel.add(createButton);
        buttonPanel.add(cancelButton);
  
        panel.add(formTitle);
        panel.add(titleLabel);
        panel.add(titleBox);
        panel.add(titleCountLabel);
        panel.add(contentLabel);
        panel.add(contentArea);
        panel.add(contentCountLabel);
        panel.add(tagsLabel);
        panel.add(tagsSection);
        panel.add(permissionLabel);
        panel.add(permissionBox);
        panel.add(usersSection);
        panel.add(buttonPanel);
      
        usersSection.setVisible(false);
    }
    
    private void setupTagsSection() {
        tagsSection.setSpacing(10);
        tagsSection.setWidth("100%");
        
        availableTagsLabel.setStyleName("form-sublabel");
        selectedTagsLabel.setStyleName("form-sublabel");
        
        availableTagsFlow.setStyleName("tags-flow");
        selectedTagsFlow.setStyleName("selected-tags-flow");
        
        tagsSection.add(availableTagsLabel);
        tagsSection.add(availableTagsFlow);
        tagsSection.add(selectedTagsLabel);
        tagsSection.add(selectedTagsFlow);
    }
    
    private void setupUsersSection() {
        usersSection.setSpacing(10);
        usersSection.setWidth("100%");
        
        availableUsersFlow.setStyleName("users-flow");
        selectedReadUsersFlow.setStyleName("selected-users-flow");
        selectedWriteUsersFlow.setStyleName("selected-users-flow");
        
        usersSection.add(readUsersLabel);
        usersSection.add(selectedReadUsersFlow);
        usersSection.add(writeUsersLabel);
        usersSection.add(selectedWriteUsersFlow);
        usersSection.add(new Label("Utenti disponibili:"));
        usersSection.add(availableUsersFlow);
    }

    
    private void loadAvailableData() {
        loadAvailableTags();
        loadAvailableUsers();
    }
    
    private void loadAvailableTags() {
        tagService.getAllTags(new AsyncCallback<List<Tag>>() {
            @Override
            public void onFailure(Throwable caught) {

                System.err.println("Errore nel caricamento tag: " + caught.getMessage());
            }
            
            @Override
            public void onSuccess(List<Tag> tags) {
                availableTags = tags;
                updateTagsDisplay();
            }
        });
    }
    
    private void loadAvailableUsers() {
        userService.getAllUsers(new AsyncCallback<List<User>>() {
            @Override
            public void onFailure(Throwable caught) {
                System.err.println("Errore nel caricamento utenti: " + caught.getMessage());
                // In caso di errore, lascia la lista vuota
                availableUsers = new ArrayList<>();
                updateUsersDisplay();
            }
            
            @Override
            public void onSuccess(List<User> users) {
                availableUsers = users;
                updateUsersDisplay();
            }
        });
    }
    
    private void updateTagsDisplay() {
        availableTagsFlow.clear();
        
        for (Tag tag : availableTags) {
            if (!selectedTags.contains(tag.getName())) {
                Button tagButton = new Button(tag.getName());
                tagButton.setStyleName("tag-button");
                tagButton.addClickHandler(event -> {
                    selectTag(tag.getName());
                });
                availableTagsFlow.add(tagButton);
            }
        }
    }
    
    private void updateSelectedTagsDisplay() {
        selectedTagsFlow.clear();
        
        for (String tagName : selectedTags) {
            HorizontalPanel tagPanel = new HorizontalPanel();
            tagPanel.setStyleName("selected-tag-panel");
            
            Label tagLabel = new Label(tagName);
            tagLabel.setStyleName("selected-tag-label");
            
            Button removeButton = new Button("×");
            removeButton.setStyleName("remove-tag-button");
            removeButton.addClickHandler(event -> {
                deselectTag(tagName);
            });
            
            tagPanel.add(tagLabel);
            tagPanel.add(removeButton);
            selectedTagsFlow.add(tagPanel);
        }
    }
    
    private void selectTag(String tagName) {
        selectedTags.add(tagName);
        updateTagsDisplay();
        updateSelectedTagsDisplay();
    }
    
    private void deselectTag(String tagName) {
        selectedTags.remove(tagName);
        updateTagsDisplay();
        updateSelectedTagsDisplay();
    }
    
    private void updateUsersDisplay() {
        availableUsersFlow.clear();
        
        for (User user : availableUsers) {
    
            if (!selectedReadUsers.contains(user.getUsername()) && 
                !selectedWriteUsers.contains(user.getUsername())) {
                
                HorizontalPanel userPanel = new HorizontalPanel();
                userPanel.setSpacing(5);
                
                Label userLabel = new Label(user.getName() + " " + user.getSurname() + " (" + user.getUsername() + ")");
                userLabel.setStyleName("user-label");

                Button readButton = new Button("Concedi lettura");
                readButton.setStyleName("user-read-button");
                readButton.addClickHandler(event -> {
                    selectUserForRead(user.getUsername());
                });
                
                Button writeButton = new Button("Concedi lettura e scrittura");
                writeButton.setStyleName("user-write-button");
                writeButton.addClickHandler(event -> {
                    selectUserForWrite(user.getUsername());
                });
                
                userPanel.add(userLabel);
                
                String selectedPermission = permissionBox.getSelectedValue();
                if (NotePermission.READ_ONLY.name().equals(selectedPermission)) {
                    userPanel.add(readButton);
                } else if (NotePermission.READ_WRITE.name().equals(selectedPermission)) {
                    userPanel.add(writeButton);
                }

                availableUsersFlow.add(userPanel);
            }
        }
    }
    
    private void updateSelectedUsersDisplay() {
        selectedReadUsersFlow.clear();
        for (String username : selectedReadUsers) {
            HorizontalPanel userPanel = createSelectedUserPanel(username, true);
            selectedReadUsersFlow.add(userPanel);
        }
        selectedWriteUsersFlow.clear();
        for (String username : selectedWriteUsers) {
            HorizontalPanel userPanel = createSelectedUserPanel(username, false);
            selectedWriteUsersFlow.add(userPanel);
        }
    }
    
    private HorizontalPanel createSelectedUserPanel(String username, boolean isReadOnly) {
        HorizontalPanel userPanel = new HorizontalPanel();
        userPanel.setStyleName("selected-user-panel");
        
        Label userLabel = new Label(username);
        userLabel.setStyleName("selected-user-label");
        
        Button removeButton = new Button("×");
        removeButton.setStyleName("remove-user-button");
        removeButton.addClickHandler(event -> {
            if (isReadOnly) {
                deselectUserFromRead(username);
            } else {
                deselectUserFromWrite(username);
            }
        });
        
        userPanel.add(userLabel);
        userPanel.add(removeButton);
        return userPanel;
    }
    
    private void selectUserForRead(String username) {
        selectedReadUsers.add(username);
        updateUsersDisplay();
        updateSelectedUsersDisplay();
    }
    
    private void selectUserForWrite(String username) {
        selectedWriteUsers.add(username);
        updateUsersDisplay();
        updateSelectedUsersDisplay();
    }
    
    private void deselectUserFromRead(String username) {
        selectedReadUsers.remove(username);
        updateUsersDisplay();
        updateSelectedUsersDisplay();
    }
    
    private void deselectUserFromWrite(String username) {
        selectedWriteUsers.remove(username);
        updateUsersDisplay();
        updateSelectedUsersDisplay();
    }
    
    private void updateUserSectionVisibility() {
        String selectedPermission = permissionBox.getSelectedValue();
        boolean showUserSection = !NotePermission.PRIVATE.name().equals(selectedPermission);
        usersSection.setVisible(showUserSection);
        
        // se i permessi sono solo lettura, nascondi la sezione scrittura
        boolean showWriteSection = NotePermission.READ_WRITE.name().equals(selectedPermission);
        writeUsersLabel.setVisible(showWriteSection);
        selectedWriteUsersFlow.setVisible(showWriteSection);
        
        // Aggiorna la visualizzazione degli utenti
        updateUsersDisplay();
    }

    public void show() {
        RootPanel.get("list").clear();
        RootPanel.get("list").add(panel);
        loadAvailableData(); 
    }

    public void clearForm() {
        titleBox.setText("");
        contentArea.setText("");
        selectedTags.clear();
        selectedReadUsers.clear();
        selectedWriteUsers.clear();
        permissionBox.setSelectedIndex(0);
        updateTagsDisplay();
        updateSelectedTagsDisplay();
        updateSelectedUsersDisplay();
        updateUserSectionVisibility();
        
        // Aggiorna i conteggi caratteri
        updateTitleCount();
        updateContentCount();
    }

    public Button getCreateButton() { return createButton; }
    public Button getCancelButton() { return cancelButton; }
    public TextBox getTitleBox() { return titleBox; }
    public TextArea getContentArea() { return contentArea; }
    public ListBox getPermissionBox() { return permissionBox; }

    public Set<String> getSelectedTags() { return new HashSet<>(selectedTags); }
    public Set<String> getSelectedReadUsers() { return new HashSet<>(selectedReadUsers); }
    public Set<String> getSelectedWriteUsers() { return new HashSet<>(selectedWriteUsers); }
 
    public String getTagsAsString() {
        return String.join(",", selectedTags);
    }
    
    public String getReadUsersAsString() {
        return String.join(",", selectedReadUsers);
    }
    
    public String getWriteUsersAsString() {
        return String.join(",", selectedWriteUsers);
    }
    
    private void updateTitleCount() {
        int length = titleBox.getText().length();
        titleCountLabel.setText(length + "/50 caratteri");
        
        if (length > 40) {
            titleCountLabel.getElement().getStyle().setColor("orange");
        } else {
            titleCountLabel.getElement().getStyle().setColor("gray");
        }
    }
    
    private void updateContentCount() {
        int length = contentArea.getText().length();
        contentCountLabel.setText(length + "/280 caratteri");
        
        if (length > 250) {
            contentCountLabel.getElement().getStyle().setColor("orange");
        } else {
            contentCountLabel.getElement().getStyle().setColor("gray");
        }
    }
}
