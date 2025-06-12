package com.google.gwt.sample.notes.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;

public class notes implements EntryPoint {
    @Override
    public void onModuleLoad() {
        /*         
        AuthServiceAsync authService = GWT.create(AuthService.class);
        // Recupero email da sessione
        authService.getCurrentEmail(new AsyncCallback<String>() {
            public void onSuccess(String email) {
                Window.alert("Utente loggato come: " + email);
                RootPanel.get("mainPanel").add(new HomePanel());
            }

            public void onFailure(Throwable caught) {
                Window.alert("Errore nel recupero email");
                RootPanel.get("mainPanel").add(new ViewNotesPanel());

            }
        });
        */

        RootPanel.get("mainPanel").add(new HomePanel());
    }
}