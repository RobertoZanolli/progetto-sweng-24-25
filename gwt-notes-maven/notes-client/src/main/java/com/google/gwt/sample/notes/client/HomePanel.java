package com.google.gwt.sample.notes.client;

import com.google.gwt.user.client.ui.*;

public class HomePanel extends VerticalPanel {
    private final Label feedbackLabel = new Label();
    private final Button loginButton = new Button("Login");
    private final Button registerButton = new Button("Registrati");

    public HomePanel() {
        buildUI();
        setupHandlers();
    }

    private void buildUI() {
        setSpacing(10);
        add(new Label("Benvenuto!"));
        add(loginButton);
        add(registerButton);
        add(feedbackLabel);
    }

    private void setupHandlers() {
        loginButton.addClickHandler(event -> {
            RootPanel.get("mainPanel").clear();
            RootPanel.get("mainPanel").add(new LoginPanel());
        });

        registerButton.addClickHandler(event -> {
            RootPanel.get("mainPanel").clear();
            RootPanel.get("mainPanel").add(new RegistrationPanel());
        });
    }
}
