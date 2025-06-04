package com.google.gwt.sample.notes.client;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.sample.notes.shared.Note;
import com.google.gwt.sample.notes.shared.Version;
import com.google.gwt.i18n.client.DateTimeFormat;

import java.util.List;

public class NoteHistory extends Composite {
    private final VerticalPanel panel = new VerticalPanel();
    private final Note note;


    public NoteHistory(Note note) {
        this.note = note;

        initWidget(panel);
        buildUI();
    }

    private void buildUI() {
        panel.setSpacing(10);
        panel.add(new Label("Cronologia delle versioni della nota:"));

        List<Version> versions = note.getAllVersions();
        DateTimeFormat fmt = DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ss");

        for (int i = 0; i < versions.size(); i++) {
            Version v = versions.get(i);
            VerticalPanel versionPanel = new VerticalPanel();
            versionPanel.setSpacing(5);
            versionPanel.setStyleName("note-history-version-panel");

            versionPanel.add(new Label("Versione " + (i + 1)));
            versionPanel.add(new Label("Titolo: " + v.getTitle()));
            versionPanel.add(new Label("Contenuto: " + v.getContent()));
            versionPanel.add(new Label("Data modifica: " + (v.getUpdatedAt() != null ? fmt.format(v.getUpdatedAt()) : "N/A")));

            panel.add(versionPanel);
        }

        Button backButton = new Button("Indietro");
        backButton.addClickHandler(event -> {
            panel.clear();
            panel.add(new NoteDetailPanel(note));
        });
        panel.add(backButton);
    }
}
