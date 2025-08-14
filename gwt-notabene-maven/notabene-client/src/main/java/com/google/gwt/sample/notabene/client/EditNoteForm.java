package com.google.gwt.sample.notabene.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.sample.notabene.shared.Note;
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

public class EditNoteForm {
    private final TagServiceAsync tagService = GWT.create(TagService.class);
    private final UserServiceAsync userService = GWT.create(UserService.class);
    
    private VerticalPanel panel = new VerticalPanel();
    private Label formTitle = new Label("Modifica nota");
    private Label titleLabel = new Label("Titolo:");
    private TextBox titleBox = new TextBox();
    private Label contentLabel = new Label("Contenuto:");
    private TextArea contentArea = new TextArea();
    private Label tagsLabel = new Label("Tag:");
    private VerticalPanel tagsSection = new VerticalPanel();
    private HorizontalPanel availableTagsPanel = new HorizontalPanel();
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
    private Button updateButton = new Button("Aggiorna Nota");
    private Button cancelButton = new Button("Annulla");
    private List<Tag> availableTags = new ArrayList<>();
    private List<User> availableUsers = new ArrayList<>();
    private Note currentNote;
    private boolean isCurrentUserAuthor = false; // Flag per controllare se l'utente corrente è l'autore

    public EditNoteForm() {
        setupForm();
        loadAvailableData();
    }

    private void setupForm() {
    panel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
    panel.setSpacing(15);
    panel.setWidth("80%");
    panel.setStyleName("form-container"); 
        formTitle.setStyleName("form-title");
        titleLabel.setStyleName("form-label");
        contentLabel.setStyleName("form-label");
        tagsLabel.setStyleName("form-label");
        permissionLabel.setStyleName("form-label");
        readUsersLabel.setStyleName("form-label");
        writeUsersLabel.setStyleName("form-label");
        titleBox.setStyleName("form-input");
        titleBox.setWidth("400px");
        contentArea.setStyleName("form-input");
        contentArea.setSize("400px", "200px");
        contentArea.setCharacterWidth(50);
        contentArea.setVisibleLines(10);
        permissionBox.setStyleName("form-input");
        permissionBox.setWidth("400px");
        updateButton.setStyleName("form-button");
    cancelButton.setStyleName("back-button form-cancel-lower");
        
        permissionBox.addItem(NotePermission.PRIVATE.getDisplayName(), NotePermission.PRIVATE.name());
        permissionBox.addItem(NotePermission.READ_ONLY.getDisplayName(), NotePermission.READ_ONLY.name());
        permissionBox.addItem(NotePermission.READ_WRITE.getDisplayName(), NotePermission.READ_WRITE.name());
        
        permissionBox.addChangeHandler(event -> {
            updateUserSectionVisibility();
        });
    
        setupTagsSection();
        setupUsersSection();
        
    buttonPanel.setSpacing(10);
    buttonPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    updateButton.getElement().getStyle().setProperty("verticalAlign", "middle");
    cancelButton.getElement().getStyle().setProperty("verticalAlign", "middle");
    buttonPanel.add(updateButton);
    buttonPanel.add(cancelButton);
        
        panel.add(formTitle);
        panel.add(titleLabel);
        panel.add(titleBox);
        panel.add(contentLabel);
        panel.add(contentArea);
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
    
    public void loadNote(Note note) {
        this.currentNote = note;
        
        titleBox.setText(note.getTitle());
        contentArea.setText(note.getContent());
        
        for (int i = 0; i < permissionBox.getItemCount(); i++) {
            if (permissionBox.getValue(i).equals(note.getPermission().name())) {
                permissionBox.setSelectedIndex(i);
                break;
            }
        }

        selectedTags.clear();
        selectedTags.addAll(note.getTags());
        
        selectedReadUsers.clear();
        selectedReadUsers.addAll(note.getReadOnlyUsers());
        
        selectedWriteUsers.clear();
        selectedWriteUsers.addAll(note.getWriteUsers());
        
        updateTagsDisplay();
        updateSelectedTagsDisplay();
        updateUsersDisplay();
        updateSelectedUsersDisplay();
        updateUserSectionVisibility();
        updatePermissionSectionVisibility();
    }
    
    // imposta se l'utente corrente è l'autore della nota
    public void setCurrentUserAuthor(boolean isAuthor) {
        this.isCurrentUserAuthor = isAuthor;
        updatePermissionSectionVisibility();
    }
    
    private void updatePermissionSectionVisibility() {
        permissionBox.setEnabled(isCurrentUserAuthor);
        
        if (!isCurrentUserAuthor) {
            permissionLabel.setText("Permessi: (solo l'autore li può modificare)");
            permissionLabel.setStyleName("form-label-disabled");
            readUsersLabel.setText("Utenti con accesso in lettura: (solo l'autore li può modificare)");
            writeUsersLabel.setText("Utenti con accesso in scrittura: (solo l'autore li può modificare)");
            readUsersLabel.setStyleName("form-label-disabled");
            writeUsersLabel.setStyleName("form-label-disabled");
        } else {
            permissionLabel.setText("Permessi:");
            permissionLabel.setStyleName("form-label");
            readUsersLabel.setText("Utenti con accesso in lettura:");
            writeUsersLabel.setText("Utenti con accesso in scrittura:");
            readUsersLabel.setStyleName("form-label");
            writeUsersLabel.setStyleName("form-label");
        }
        
        updateUsersDisplay();
        updateSelectedUsersDisplay();
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
                readButton.setEnabled(isCurrentUserAuthor); 
                readButton.addClickHandler(event -> {
                    if (isCurrentUserAuthor) {
                        selectUserForRead(user.getUsername());
                    }
                });
                
                Button writeButton = new Button("Concedi lettura e scrittura");
                writeButton.setStyleName("user-write-button");
                writeButton.setEnabled(isCurrentUserAuthor);
                writeButton.addClickHandler(event -> {
                    if (isCurrentUserAuthor) {
                        selectUserForWrite(user.getUsername());
                    }
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
        removeButton.setEnabled(isCurrentUserAuthor); 
        removeButton.addClickHandler(event -> {
            if (isCurrentUserAuthor) {
                if (isReadOnly) {
                    deselectUserFromRead(username);
                } else {
                    deselectUserFromWrite(username);
                }
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
        
        boolean showWriteSection = NotePermission.READ_WRITE.name().equals(selectedPermission);
        writeUsersLabel.setVisible(showWriteSection);
        selectedWriteUsersFlow.setVisible(showWriteSection);
        
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
        currentNote = null;
        isCurrentUserAuthor = false;
        updateTagsDisplay();
        updateSelectedTagsDisplay();
        updateSelectedUsersDisplay();
        updateUserSectionVisibility();
        updatePermissionSectionVisibility();
    }
    
    public Button getUpdateButton() { return updateButton; }
    public Button getCancelButton() { return cancelButton; }
    public TextBox getTitleBox() { return titleBox; }
    public TextArea getContentArea() { return contentArea; }
    public ListBox getPermissionBox() { return permissionBox; }
    public Note getCurrentNote() { return currentNote; }
    
    public Set<String> getSelectedTags() { return new HashSet<>(selectedTags); }
    public Set<String> getSelectedReadUsers() { return new HashSet<>(selectedReadUsers); }
    public Set<String> getSelectedWriteUsers() { return new HashSet<>(selectedWriteUsers); }
}