package com.google.gwt.sample.notabene.client;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.RootPanel;

public class LoginForm {
    private VerticalPanel panel = new VerticalPanel();
    private Label loginTitle = new Label("Accesso");
    private Label usernameLabel = new Label("Username:");
    private TextBox usernameBox = new TextBox();
    private Label passwordLabel = new Label("Password:");
    private PasswordTextBox passwordBox = new PasswordTextBox();
    private Button confirmButton = new Button("Accedi");
    private Button backButton = new Button("Torna alla Home");

    public LoginForm() {
        panel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
        panel.setSpacing(15);
        panel.setWidth("100%");
        loginTitle.setStyleName("form-title");
        usernameLabel.setStyleName("form-label");
        passwordLabel.setStyleName("form-label");
        usernameBox.setStyleName("form-input");
        passwordBox.setStyleName("form-input");
        confirmButton.setStyleName("form-button");
        backButton.setStyleName("back-button");
        panel.add(loginTitle);
        panel.add(usernameLabel);
        panel.add(usernameBox);
        panel.add(passwordLabel);
        panel.add(passwordBox);
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
}
