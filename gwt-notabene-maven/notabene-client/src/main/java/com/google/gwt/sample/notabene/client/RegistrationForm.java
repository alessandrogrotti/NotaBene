package com.google.gwt.sample.notabene.client;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.RootPanel;

public class RegistrationForm {
    private VerticalPanel panel = new VerticalPanel();
    private Label registerTitle = new Label("Registrazione");
    private Label usernameLabel = new Label("Username:");
    private TextBox usernameBox = new TextBox();
    private Label passwordLabel = new Label("Password:");
    private PasswordTextBox passwordBox = new PasswordTextBox();
    private Label nameLabel = new Label("Nome:");
    private TextBox nameBox = new TextBox();
    private Label surnameLabel = new Label("Cognome:");
    private TextBox surnameBox = new TextBox();
    private Button confirmButton = new Button("Conferma Registrazione");
    private Button backButton = new Button("Torna alla Home");

    public RegistrationForm() {
        panel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
        panel.setSpacing(15);
        panel.setWidth("100%");
        registerTitle.setStyleName("form-title");
        usernameLabel.setStyleName("form-label");
        passwordLabel.setStyleName("form-label");
        nameLabel.setStyleName("form-label");
        surnameLabel.setStyleName("form-label");
        usernameBox.setStyleName("form-input");
        passwordBox.setStyleName("form-input");
        nameBox.setStyleName("form-input");
        surnameBox.setStyleName("form-input");
        confirmButton.setStyleName("form-button");
        backButton.setStyleName("back-button");
        panel.add(registerTitle);
        panel.add(usernameLabel);
        panel.add(usernameBox);
        panel.add(passwordLabel);
        panel.add(passwordBox);
        panel.add(nameLabel);
        panel.add(nameBox);
        panel.add(surnameLabel);
        panel.add(surnameBox);
        panel.add(confirmButton);
        panel.add(backButton);
    }

    public void show() {
        RootPanel.get("list").clear();
        RootPanel.get("list").add(panel);
    }

    public Button getConfirmButton() { return confirmButton; }
    public Button getBackButton() { return backButton; }
    public TextBox getUsernameBox() { return usernameBox; }
    public PasswordTextBox getPasswordBox() { return passwordBox; }
    public TextBox getNameBox() { return nameBox; }
    public TextBox getSurnameBox() { return surnameBox; }
}
