package com.google.gwt.sample.notes.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point dell'applicazione
 */
public class notes implements EntryPoint {
    @Override
    public void onModuleLoad() {
        RootPanel.get("mainPanel").clear();
        RootPanel.get("mainPanel").add(new HomePanel());
    }
}