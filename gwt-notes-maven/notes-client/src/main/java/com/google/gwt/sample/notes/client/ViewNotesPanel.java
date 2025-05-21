package com.google.gwt.sample.notes.client;

import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.sample.notes.shared.Note;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Il sistema deve permettere a un utente registrato di creare nuove note testuali (max 280 caratteri).
 * Ogni nota può avere un titolo, un contenuto testuale, la data di creazione, la data di ultima modifica e un utente proprietario.
 * Deve essere possibile organizzare le note in cartelle oppure assegnare dei tag. In modo da poter raggruppare le note dello stesso tipo.
 */
public class ViewNotesPanel extends Composite {
    private VerticalPanel panel = new VerticalPanel();
    private List<Note> notes;

    public ViewNotesPanel(){}

    public ViewNotesPanel(List<Note> notes) {
        this.notes = notes;
        initWidget(panel);
        renderNotes();
    }

    private void renderNotes() {
        panel.clear();
        for (Note note : notes) {
            HorizontalPanel notePanel = new HorizontalPanel();
            notePanel.add((IsWidget) new Label(note.getTitle() + ": " + note.getContent()));
            notePanel.add((IsWidget) new Label("Ultima modifica: " + note.getLastModifiedDate().toString()));
            panel.add(notePanel);
        }
    }
}
