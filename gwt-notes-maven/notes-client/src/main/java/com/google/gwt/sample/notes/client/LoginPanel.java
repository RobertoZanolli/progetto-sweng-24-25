package com.google.gwt.sample.notes.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.*;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;

public class LoginPanel extends VerticalPanel {
    private final TextBox emailBox = new TextBox();
    private final PasswordTextBox passwordBox = new PasswordTextBox();
    private final Label feedbackLabel = new Label();
    private final Button loginButton = new Button("Login");

    public LoginPanel() {
        setSpacing(10);
        add(new Label("Login"));
        add(new Label("Email:"));
        add(emailBox);
        add(new Label("Password:"));
        add(passwordBox);
        add(loginButton);
        add(feedbackLabel);

        loginButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                doLogin();
            }
        });
    }

    private void doLogin() {
        String email = emailBox.getText().trim();
        String password = passwordBox.getText();

        if (email.isEmpty() || password.isEmpty()) {
            feedbackLabel.setText("Email and password required.");
            return;
        }
        loginButton.setEnabled(false);
        feedbackLabel.setText("Logging in...");

        JSONObject payload = new JSONObject();
        payload.put("email", new JSONString(email));
        payload.put("password", new JSONString(password));

        RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, GWT.getHostPageBaseURL() + "api/login");
        builder.setHeader("Content-Type", "application/json");
        try {
            builder.sendRequest(payload.toString(), new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {
                    loginButton.setEnabled(true);
                    if (response.getStatusCode() == Response.SC_OK) {
                        feedbackLabel.setText("Login successful!");
                    } else {
                        feedbackLabel.setText("Login failed: " + response.getText());
                    }
                }
                @Override
                public void onError(Request request, Throwable exception) {
                    loginButton.setEnabled(true);
                    feedbackLabel.setText("Error: " + exception.getMessage());
                }
            });
        } catch (RequestException e) {
            loginButton.setEnabled(true);
            feedbackLabel.setText("Request error: " + e.getMessage());
        }
    }
}
