package com.google.gwt.sample.notes.client;


import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.sample.notes.shared.Note;


public class NoteDetailPanel extends Composite {

    private final VerticalPanel panel = new VerticalPanel();
    private final Note note; // Nota da visualizzare/modificare
    private final Label feedbackLabel = new Label();
    private final TextBox titleBox = new TextBox();
    private final TextArea contentBox = new TextArea();
    private final Label tagsLabel = new Label();
    private final Label createdDateLabel = new Label();
    private final Label lastModifiedDateLabel = new Label();
    private final Button deleteButton = new Button("Elimina");
    private final Button editButton = new Button("Modifica");
    private final Button duplicateButton = new Button("Duplica");
    private final Button backButton = new Button("Indietro");

    public NoteDetailPanel(Note note) {
        this.note = note;
        initWidget(panel);
        buildUI();
        setupHandlers();
    }

    private void buildUI() {
        panel.setSpacing(10);

        panel.add(new Label("Titolo:"));
        titleBox.setText(note.getTitle() != null ? note.getTitle() : "N/A");
        titleBox.setEnabled(false); // Inizialmente non modificabile
        panel.add(titleBox);

        panel.add(new Label("Contenuto:"));
        contentBox.setText(note.getContent() != null ? note.getContent() : "N/A");
        contentBox.setVisibleLines(5);
        contentBox.setCharacterWidth(40);
        contentBox.setEnabled(false); // Inizialmente non modificabile
        panel.add(contentBox);

        String tags = note.getTags() != null && note.getTags().length > 0 
            ? String.join(", ", note.getTags()) : "Nessun tag";
        tagsLabel.setText("Tag: " + tags);
        panel.add(tagsLabel);

        // CreatedDate
        createdDateLabel.setText("Creato: " + (note.getCreatedDate() != null 
            ? note.getCreatedDate().toString() : "Data non disponibile"));
        panel.add(createdDateLabel);

        // LastModifiedDate
        lastModifiedDateLabel.setText("Ultima modifica: " + (note.getLastModifiedDate() != null 
            ? note.getLastModifiedDate().toString() : "Data non disponibile"));
        panel.add(lastModifiedDateLabel);


        HorizontalPanel buttonPanel = new HorizontalPanel();
        buttonPanel.setSpacing(10);
        buttonPanel.add(deleteButton);
        buttonPanel.add(editButton);
        buttonPanel.add(duplicateButton);
        buttonPanel.add(backButton);
        panel.add(buttonPanel);

        panel.add(feedbackLabel);
    }

    private void setupHandlers() {
        backButton.addClickHandler(event -> {
            panel.clear();
            panel.add(new ViewNotesPanel());
        });
    }
}