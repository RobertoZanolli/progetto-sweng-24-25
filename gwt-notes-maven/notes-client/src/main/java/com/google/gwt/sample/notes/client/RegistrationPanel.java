package com.google.gwt.sample.notes.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.*;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;

/**
 * Pannello per la registrazione degli utenti
 */
public class RegistrationPanel extends Composite {
    private final VerticalPanel panel = new VerticalPanel();
    private final TextBox emailBox = new TextBox();
    private final PasswordTextBox passwordBox = new PasswordTextBox();
    private final Label feedbackLabel = new Label();
    private final Button registerButton = new Button("Registrati");
    private final Button backButton = new Button("Indietro");

    public RegistrationPanel() {
        initWidget(panel);
        buildUI();
        setupHandlers();
    }

    private void buildUI() {
        panel.setSpacing(10);
        panel.add(new Label("Registrazione"));
        panel.add(new Label("Email:"));
        panel.add(emailBox);
        panel.add(new Label("Password:"));
        panel.add(passwordBox);
        panel.add(registerButton);
        panel.add(feedbackLabel);
        panel.add(backButton);
    }

    private void setupHandlers() {
        registerButton.addClickHandler(event -> {
            doRegister();
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
            feedbackLabel.setText("Email e password obbligatorie.");
        } else if (!email.contains("@")) {
            feedbackLabel.setText("L'email deve contenere il simbolo '@'.");
        } else {
            registerButton.setEnabled(false);
            feedbackLabel.setText("Registrazione in corso...");

            JSONObject payload = new JSONObject();
            payload.put("email", new JSONString(email));
            payload.put("password", new JSONString(password));

            RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, GWT.getHostPageBaseURL() + "api/register");
            builder.setHeader("Content-Type", "application/json");
            builder.setIncludeCredentials(true);
            try {
                builder.sendRequest(payload.toString(), new RequestCallback() {
                    @Override
                    public void onResponseReceived(Request request, Response response) {
                        registerButton.setEnabled(true);
                        if (response.getStatusCode() == Response.SC_OK) {
                            feedbackLabel.setText("Registrazione completata!");
                            RootPanel.get("mainPanel").clear();
                            RootPanel.get("mainPanel").add(new LoginPanel());
                        } else if (response.getStatusCode() == Response.SC_CONFLICT) {
                            feedbackLabel.setText("Utente già esistente.");
                        } else {
                            feedbackLabel.setText("Registrazione fallita: " + response.getText());
                        }
                    }
                    @Override
                    public void onError(Request request, Throwable exception) {
                        registerButton.setEnabled(true);
                        feedbackLabel.setText("Errore: " + exception.getMessage());
                    }
                });
            } catch (RequestException e) {
                registerButton.setEnabled(true);
                feedbackLabel.setText("Errore nella richiesta: " + e.getMessage());
            }
        }
    }
}