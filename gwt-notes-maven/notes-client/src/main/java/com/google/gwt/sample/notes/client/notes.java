package com.google.gwt.sample.notes.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;

public class notes implements EntryPoint {
    @Override
    public void onModuleLoad() {
        // Vi serve per aggiungere tutti i vari pannelli se vogliamo fare una single page app
        //RootPanel.get("mainPanel").add(new CreateNotePanel());
        //RootPanel.get("mainPanel").add(new RegistrationPanel());
        //RootPanel.get("mainPanel").add(new ViewNotesPanel());
        //RootPanel.get("mainPanel").add(new RegistrationPanel());
        //RootPanel.get("mainPanel").add(new LoginPanel());
        RootPanel.get("mainPanel").add(new HomePanel());

        /*
         * ToDo: implementare controllo per utente già loggato, così
         * quando ricarica la pagina non viene reindirizzato alla home
         * ma alla pagina di visualizzazione note
         * 
         * ToDo: aggiungere controllo per utenti non loggati
         * per impedire accesso alle rotte senza login
         */
    }
}