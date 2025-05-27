package com.google.gwt.sample.notes.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.*;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;

public class RegistrationPanel extends VerticalPanel {
    private final TextBox emailBox = new TextBox();
    private final PasswordTextBox passwordBox = new PasswordTextBox();
    private final Label feedbackLabel = new Label();
    private final Button registerButton = new Button("Registrati");
    private final Button backButton = new Button("Indietro");

    public RegistrationPanel() {
        setSpacing(10);
        add(new Label("Registrazione"));
        add(new Label("Email:"));
        add(emailBox);
        add(new Label("Password:"));
        add(passwordBox);
        add(registerButton);
        add(feedbackLabel);
        add(backButton);

        registerButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                doRegister();
            }
        });

        backButton.addClickHandler(event -> {
            RootPanel.get("mainPanel").clear();
            RootPanel.get("mainPanel").add(new HomePanel());
        });
    }

    private void doRegister() {
        String email = emailBox.getText().trim();
        String password = passwordBox.getText();

        if (email.isEmpty() || password.isEmpty()) {
            feedbackLabel.setText("Email and password required.");
        } else if (!email.contains("@")) {
            feedbackLabel.setText("Email must contain '@' symbol.");
        } else {
            registerButton.setEnabled(false);
            feedbackLabel.setText("Registering...");

            JSONObject payload = new JSONObject();
            payload.put("email", new JSONString(email));
            payload.put("password", new JSONString(password));

            RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, GWT.getHostPageBaseURL() + "api/register");
            builder.setHeader("Content-Type", "application/json");
            try {
                builder.sendRequest(payload.toString(), new RequestCallback() {
                    @Override
                    public void onResponseReceived(Request request, Response response) {
                        registerButton.setEnabled(true);
                        if (response.getStatusCode() == Response.SC_OK) {
                            feedbackLabel.setText("Registration successful!");
                            RootPanel.get("mainPanel").clear();
                            RootPanel.get("mainPanel").add(new LoginPanel());
                        } else if (response.getStatusCode() == Response.SC_CONFLICT) {
                            feedbackLabel.setText("User already exists.");
                        } else {
                            feedbackLabel.setText("Registration failed: " + response.getText());
                        }
                    }
                    @Override
                    public void onError(Request request, Throwable exception) {
                        registerButton.setEnabled(true);
                        feedbackLabel.setText("Error: " + exception.getMessage());
                    }
                });
            } catch (RequestException e) {
                registerButton.setEnabled(true);
                feedbackLabel.setText("Request error: " + e.getMessage());
            }
        }
    }
}