package com.google.gwt.sample.notabene.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.sample.notabene.shared.User;
import com.google.gwt.sample.notabene.shared.UserService;
import com.google.gwt.sample.notabene.shared.UserServiceAsync;
import com.google.gwt.sample.notabene.shared.Tag;
import com.google.gwt.sample.notabene.shared.TagService;
import com.google.gwt.sample.notabene.shared.TagServiceAsync;
import com.google.gwt.sample.notabene.shared.Note;
import com.google.gwt.sample.notabene.shared.NotePermission;
import com.google.gwt.sample.notabene.shared.NoteService;
import com.google.gwt.sample.notabene.shared.NoteServiceAsync;
 
public class NotaBene implements EntryPoint {

    private final NoteServiceAsync noteService = GWT.create(NoteService.class);
    private final UserServiceAsync userService = GWT.create(UserService.class);
    private User currentUser = null;
    private HomePage homePage;
    private RegistrationForm registrationForm;
    private LoginForm loginForm;
    private TagManagementPage tagManagementPage;
    private CreateNoteForm createNoteForm;
    private EditNoteForm editNoteForm;
    private NoteDetailPage noteDetailPage;
    private final TagServiceAsync tagService = GWT.create(TagService.class);
 
    //ENTRY POINT HOME PAGE BASIC
    public void onModuleLoad() {
        homePage = new HomePage();
        registrationForm = new RegistrationForm();
        loginForm = new LoginForm();
        tagManagementPage = new TagManagementPage();
        createNoteForm = new CreateNoteForm();
        editNoteForm = new EditNoteForm();
        noteDetailPage = new NoteDetailPage();
        setupEventHandlers();
        showHomePage();
    }

    private void showRegistrationForm() {
        registrationForm.show();
    }

    private void showLoginForm() {
        loginForm.show();
    }
  
    private void showHomePage() {
        boolean isAuthenticated = currentUser != null;
        homePage.show(isAuthenticated, currentUser);
    }

    private void showTagManagementPage() {
        if (currentUser != null) {
            tagManagementPage.show();
        }
        editNoteForm = new EditNoteForm();
    }
    
    private void showCreateNoteForm() {
        if (currentUser != null) {
            createNoteForm.show();
        }
    }

    private void showEditNoteForm(Note note) {
        if (editNoteForm == null) {
            editNoteForm = new EditNoteForm();
        }
        editNoteForm.loadNote(note);
        
        // Controlla se l'utente corrente è l'autore della nota
        boolean isAuthor = currentUser != null && currentUser.getUsername().equals(note.getOwnerUsername());
        editNoteForm.setCurrentUserAuthor(isAuthor);
        
        editNoteForm.show();
    }

    private void showNoteDetail(Note note) {
        if (currentUser != null && note != null) {
            noteDetailPage.showNote(note);
            noteDetailPage.setDeleteButtonVisible(true, currentUser.getUsername());
            noteDetailPage.show();
        }
    }
    
    private void handleDeleteNote(Note note) {
        if (currentUser == null || note == null) {
            return;
        }

        // conferma eliminazione
        boolean confirm = Window.confirm("Sei sicuro di voler eliminare la nota '" + note.getTitle() + "'?\nQuesta operazione non può essere annullata.");
        
        if (!confirm) {
            return;
        }
        
        // call a servizio per eliminare la nota
        noteService.deleteNote(note.getId(), currentUser.getUsername(), new AsyncCallback<Boolean>() {
            @Override
            public void onFailure(Throwable caught) {
                Window.alert("Errore durante l'eliminazione della nota: " + caught.getMessage());
            }
            
            @Override
            public void onSuccess(Boolean result) {
                if (result) {
                    Window.alert("Nota eliminata con successo!");
                    showHomePage();
                } else {
                    Window.alert("Impossibile eliminare la nota. Verifica di avere i permessi necessari.");
                }
            }
        });
    }
    
    private void handleRegistration() {
        String username = registrationForm.getUsernameBox().getText().trim();
        String password = registrationForm.getPasswordBox().getText();
        String name = registrationForm.getNameBox().getText().trim();
        String surname = registrationForm.getSurnameBox().getText().trim();
        if (username.isEmpty() || password.isEmpty() || name.isEmpty() || surname.isEmpty()) {
            Window.alert("Tutti i campi sono obbligatori!");
            return;
        }
        if (password.length() < 4) {
            Window.alert("Password deve essere di almeno 4 caratteri!");
            return;
        }
        User newUser = new User(username, password, name, surname);
        Button confirmRegButton = registrationForm.getConfirmButton();
        confirmRegButton.setEnabled(false);
        confirmRegButton.setText("Registrazione in corso...");
        userService.registerUser(newUser, new AsyncCallback<Boolean>() {
            @Override
            public void onFailure(Throwable caught) {
                confirmRegButton.setEnabled(true);
                confirmRegButton.setText("Conferma Registrazione");
                Window.alert("Errore durante la registrazione: " + caught.getMessage());
            }
            @Override
            public void onSuccess(Boolean result) {
                confirmRegButton.setEnabled(true);
                confirmRegButton.setText("Conferma Registrazione");
                if (result) {
                    Window.alert("Registrazione completata con successo per: " + name + " " + surname);
                    clearRegistrationForm();
                    showHomePage();
                } else {
                    Window.alert("Errore durante la registrazione. Riprova più tardi.");
                }
            }
        });
    }

   private void handleCreateNote() {
        String title = createNoteForm.getTitleBox().getText().trim();
        String content = createNoteForm.getContentArea().getText().trim();
        String permissionValue = createNoteForm.getPermissionBox().getSelectedValue();
        //controlli lato client
        if (title.isEmpty() || content.isEmpty()) {
            Window.alert("Titolo e contenuto sono obbligatori!");
            return;
        }
        
        if (currentUser == null) {
            Window.alert("Utente non autenticato!");
            return;
        }

        Note note = new Note(title, content, currentUser.getUsername());
        //impostazione dei permessi
        try {
            NotePermission permission = NotePermission.valueOf(permissionValue);
            note.setPermission(permission);
        } catch (Exception e) {
            note.setPermission(NotePermission.PRIVATE);
        }
        
        for (String tag : createNoteForm.getSelectedTags()) {
            note.addTag(tag);
        }

        if (!NotePermission.PRIVATE.name().equals(permissionValue)) {
            for (String user : createNoteForm.getSelectedReadUsers()) {
                note.getReadOnlyUsers().add(user);
            }
        }

        if (NotePermission.READ_WRITE.name().equals(permissionValue)) {
            for (String user : createNoteForm.getSelectedWriteUsers()) {
                note.getWriteUsers().add(user);
            }
        }
        
        // disabilito il pulsante per evitare doppi invii
        Button createButton = createNoteForm.getCreateButton();
        createButton.setEnabled(false);
        createButton.setText("Creazione in corso...");
        
        // chiama il servizio per creare la nota
        noteService.createNote(note, new AsyncCallback<Boolean>() {
            @Override
            public void onFailure(Throwable caught) {
                createButton.setEnabled(true);
                createButton.setText("Crea Nota");
                Window.alert("Errore durante la creazione della nota: " + caught.getMessage());
            }
            
            @Override
            public void onSuccess(Boolean result) {
                createButton.setEnabled(true);
                createButton.setText("Crea Nota");
                
                if (result) {
                    Window.alert("Nota creata con successo!");
                    createNoteForm.clearForm();
                    showHomePage();
                } else {
                    Window.alert("Errore durante la creazione della nota. Riprova più tardi.");
                }
            }
        });
    }

    private void handleLogin() {
        String username = loginForm.getUsernameBox().getText().trim();
        String password = loginForm.getPasswordBox().getText();
        if (username.isEmpty() || password.isEmpty()) {
            Window.alert("Username e password sono obbligatori!");
            return;
        }
        Button confirmLoginButton = loginForm.getConfirmButton();
        confirmLoginButton.setEnabled(false);
        confirmLoginButton.setText("Accesso in corso");
        userService.authenticateUser(username, password, new AsyncCallback<User>() {
            @Override
            public void onFailure(Throwable caught) {
                confirmLoginButton.setEnabled(true);
                confirmLoginButton.setText("Accedi");
                Window.alert("Errore durante l'accesso: " + caught.getMessage());
            }
            @Override
            public void onSuccess(User result) {
                confirmLoginButton.setEnabled(true);
                confirmLoginButton.setText("Accedi");
                if (result != null) {
                    Window.alert("Accesso effettuato con successo!\n" +
                               "Benvenuto " + result.getName() + " " + result.getSurname() + "!");
                    clearLoginForm();
                    currentUser = result;
                    showHomePage();
                } else {
                    Window.alert("Username o password non corretti!");
                }
            }
        });
    }

    private void handleUpdateNote() {
        String title = editNoteForm.getTitleBox().getText().trim();
        String content = editNoteForm.getContentArea().getText().trim();
        String permissionValue = editNoteForm.getPermissionBox().getSelectedValue();
        Note currentNote = editNoteForm.getCurrentNote();
        
        if (title.isEmpty() || content.isEmpty()) {
            Window.alert("Titolo e contenuto sono obbligatori!");
            return;
        }
        
        if (currentUser == null) {
            Window.alert("Utente non autenticato!");
            return;
        }
        
        if (currentNote == null) {
            Window.alert("Errore: nota da modificare non trovata!");
            return;
        }
        
        if (!currentNote.canWrite(currentUser.getUsername())) {
            Window.alert("Non hai i permessi per modificare questa nota!");
            return;
        }
        
        currentNote.setTitle(title);
        currentNote.setContent(content);
        
        boolean isAuthor = currentUser.getUsername().equals(currentNote.getOwnerUsername());
        if (isAuthor) {
            try {
                NotePermission permission = NotePermission.valueOf(permissionValue);
                currentNote.setPermission(permission);
            } catch (Exception e) {
                currentNote.setPermission(NotePermission.PRIVATE);
            }
            
            currentNote.getReadOnlyUsers().clear();
            currentNote.getWriteUsers().clear();
            
            if (!NotePermission.PRIVATE.name().equals(permissionValue)) {
                for (String user : editNoteForm.getSelectedReadUsers()) {
                    currentNote.getReadOnlyUsers().add(user);
                }
            }
            
            if (NotePermission.READ_WRITE.name().equals(permissionValue)) {
                for (String user : editNoteForm.getSelectedWriteUsers()) {
                    currentNote.getWriteUsers().add(user);
                }
            }
        }
        
        currentNote.getTags().clear();
        for (String tag : editNoteForm.getSelectedTags()) {
            currentNote.addTag(tag);
        }
        
        Button updateButton = editNoteForm.getUpdateButton();
        updateButton.setEnabled(false);
        updateButton.setText("Aggiornamento in corso...");
        
        noteService.updateNote(currentNote, currentUser.getUsername(), new AsyncCallback<Boolean>() {
            @Override
            public void onFailure(Throwable caught) {
                updateButton.setEnabled(true);
                updateButton.setText("Aggiorna Nota");
                
                Window.alert("Errore durante l'aggiornamento della nota: " + caught.getMessage());
            }
            
            @Override
            public void onSuccess(Boolean result) {
                updateButton.setEnabled(true);
                updateButton.setText("Aggiorna Nota");
                
                if (result) {
                    Window.alert("Nota aggiornata con successo!");
                    editNoteForm.clearForm();
                    showHomePage();
                } else {
                    Window.alert("Errore durante l'aggiornamento della nota. Riprova più tardi.");
                }
            }
        });
    }

    //Pulisce i campi del form di login
    private void clearLoginForm() {
        loginForm.getUsernameBox().setText("");
        loginForm.getPasswordBox().setText("");
    }
    
    private void clearRegistrationForm() {
        registrationForm.getUsernameBox().setText("");
        registrationForm.getPasswordBox().setText("");
        registrationForm.getNameBox().setText("");
        registrationForm.getSurnameBox().setText("");
    }
   
   private void setupEventHandlers() {
        homePage.getRegisterButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                showRegistrationForm();
            }
        });

        homePage.getLoginButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                showLoginForm();
            }
        });

        homePage.setEditNoteClickHandler(new HomePage.EditNoteClickHandler() {
            @Override
            public void onEditNote(Note note) {
                showEditNoteForm(note);
            }
        });

        registrationForm.getConfirmButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                handleRegistration();
            }
        });

        registrationForm.getBackButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                showHomePage();
            }
        });

        loginForm.getConfirmButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                handleLogin();
            }
        });

        loginForm.getBackButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                showHomePage();
            }
        });
        
        homePage.getManageTagsButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                showTagManagementPage();
            }
        });
        
        homePage.getAddNoteButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                showCreateNoteForm();
            }
        });
        
        homePage.getLogoutButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                currentUser = null;
                Window.alert("Logout effettuato con successo!");
                showHomePage();
            }
        });
        
        tagManagementPage.getBackButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                showHomePage();
            }
        });
        
        createNoteForm.getCancelButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                createNoteForm.clearForm();
                showHomePage();
            }
        });
        
        createNoteForm.getCreateButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
               handleCreateNote();
            }
        });

        // handler per il pulsante "Torna alla Lista" nella pagina di dettaglio
        noteDetailPage.getBackButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                showHomePage();
            }
        });

        // handler per il pulsante "Elimina" nella pagina di dettaglio
        noteDetailPage.getDeleteButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                handleDeleteNote(noteDetailPage.getCurrentNote());
            }
        });

        // handler per i click sulle note nella homepage
        homePage.setNoteClickHandler(new HomePage.NoteClickHandler() {
            @Override
            public void onNoteClick(Note note) {
                showNoteDetail(note);
            }
        });

        // handler per l'eliminazione delle note dalla homepage
        homePage.setDeleteNoteHandler(new HomePage.DeleteNoteHandler() {
            @Override
            public void onDeleteNote(Note note) {
                handleDeleteNote(note);
            }
        });

        // handler per la modifica delle note
        editNoteForm.getUpdateButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                handleUpdateNote();
            }
        });
        
        editNoteForm.getCancelButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                editNoteForm.clearForm();
                showHomePage();
            }
        });
    }
}