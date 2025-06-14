package com.google.gwt.sample.notes.client;

import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.ui.RootPanel;

public class HomePanel extends Composite {
    private final VerticalPanel panel = new VerticalPanel();
    private final Label feedbackLabel = new Label();
    private final Button loginButton = new Button("Login");
    private final Button registerButton = new Button("Registrati");

    public HomePanel() {
        initWidget(panel);
        buildUI();
        setupHandlers();
    }

    private void buildUI() {
        panel.setSpacing(10);
        panel.add(new Label("Benvenuto!"));
        panel.add(loginButton);
        panel.add(registerButton);
        panel.add(feedbackLabel);
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
