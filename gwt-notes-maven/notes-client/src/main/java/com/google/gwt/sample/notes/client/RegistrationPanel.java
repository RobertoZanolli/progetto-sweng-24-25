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
    private final Button registerButton = new Button("Register");

    public RegistrationPanel() {
        setSpacing(10);
        add(new Label("Register New Account"));
        add(new Label("Email:"));
        add(emailBox);
        add(new Label("Password:"));
        add(passwordBox);
        add(registerButton);
        add(feedbackLabel);

        registerButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                doRegister();
            }
        });
    }

    private void doRegister() {
        String username = emailBox.getText().trim();
        String password = passwordBox.getText();
        if (username.isEmpty() || password.isEmpty()) {
            feedbackLabel.setText("Email and password required.");
            return;
        }
        registerButton.setEnabled(false);
        feedbackLabel.setText("Registering...");

        JSONObject payload = new JSONObject();
        payload.put("email", new JSONString(username));
        payload.put("passwordHash", new JSONString(password));

        //Regaz ricordatevi di sistemare sempre le entry point
        RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, GWT.getHostPageBaseURL() + "api/register");
        builder.setHeader("Content-Type", "application/json");
        try {
            builder.sendRequest(payload.toString(), new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {
                    registerButton.setEnabled(true);
                    if (response.getStatusCode() == Response.SC_OK) {
                        feedbackLabel.setText("Registration successful!");
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