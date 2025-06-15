package com.google.gwt.sample.notes.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.*;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;

public class LoginPanel extends Composite {
    private final VerticalPanel panel = new VerticalPanel();
    private final TextBox emailBox = new TextBox();
    private final PasswordTextBox passwordBox = new PasswordTextBox();
    private final Label feedbackLabel = new Label();
    private final Button loginButton = new Button("Accedi");
    private final Button backButton = new Button("Indietro");

    public LoginPanel() {
        initWidget(panel);
        buildUI();
        setupHandlers();
    }

    private void buildUI() {
        panel.setSpacing(10);
        panel.add(new Label("Accesso"));
        panel.add(new Label("Email:"));
        panel.add(emailBox);
        panel.add(new Label("Password:"));
        panel.add(passwordBox);
        panel.add(loginButton);
        panel.add(feedbackLabel);
        panel.add(backButton);
    }

    private void setupHandlers() {
        loginButton.addClickHandler(event -> {
            doLogin();
        });

        backButton.addClickHandler(event -> {
            RootPanel.get("mainPanel").clear();
            RootPanel.get("mainPanel").add(new HomePanel());
        });
    }

    private void doLogin() {
        String email = emailBox.getText().trim();
        String password = passwordBox.getText();

        if (email.isEmpty() || password.isEmpty()) {
            feedbackLabel.setText("Email e password obbligatorie.");
            return;
        }
        loginButton.setEnabled(false);
        feedbackLabel.setText("Accesso in corso...");

        JSONObject payload = new JSONObject();
        payload.put("email", new JSONString(email));
        payload.put("password", new JSONString(password));

        RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, GWT.getHostPageBaseURL() + "api/login");
        builder.setHeader("Content-Type", "application/json");
        builder.setIncludeCredentials(true);
        try {
            builder.sendRequest(payload.toString(), new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {
                    loginButton.setEnabled(true);
                    if (response.getStatusCode() == Response.SC_OK) {
                        feedbackLabel.setText("Login successful!");
                        Session.getInstance().setUserEmail(email); // salva la mail
                        RootPanel.get("mainPanel").clear();
                        RootPanel.get("mainPanel").add(new ViewNotesPanel());
                    } else {
                        feedbackLabel.setText("Login fallito: utente non registrato");
                    }
                }

                @Override
                public void onError(Request request, Throwable exception) {
                    loginButton.setEnabled(true);
                    feedbackLabel.setText("Errore: " + exception.getMessage());
                }
            });
        } catch (RequestException e) {
            loginButton.setEnabled(true);
            feedbackLabel.setText("Errore nella richiesta: " + e.getMessage());
            return;
        }
    }
}
