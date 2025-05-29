package com.google.gwt.sample.notes.client;


import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.sample.notes.shared.Note;
import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONArray;


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
        createdDateLabel.setText("Creata: " + (note.getCreatedDate() != null 
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

        deleteButton.addClickHandler(event -> {
            String noteId = note.getId();

            String url = GWT.getHostPageBaseURL() + "api/notes?id=" + noteId;
            RequestBuilder builder = new RequestBuilder(RequestBuilder.DELETE, url);
            builder.setHeader("Content-Type", "application/json");
            try {
                builder.sendRequest(null, new RequestCallback() {
                    @Override
                    public void onResponseReceived(Request request, Response response) {
                        if (response.getStatusCode() == Response.SC_OK) {
                            feedbackLabel.setText("Nota eliminata!");
                            panel.clear();
                            panel.add(new ViewNotesPanel());
                        } else {
                            feedbackLabel.setText("Eliminazione fallita: " + response.getText());
                        }
                    }

                    @Override
                    public void onError(Request request, Throwable exception) {
                        feedbackLabel.setText("Errore durante l'eliminazione: " + exception.getMessage());
                    }
                });
            } catch (RequestException e) {
                feedbackLabel.setText("Request error: " + e.getMessage());
            }
        });

        editButton.addClickHandler(event -> {
            // ToDo: collegare EditNotePanel (ToDo)
        });

        duplicateButton.addClickHandler(event -> {
            JSONObject payload = new JSONObject();
            payload.put("title", new JSONString(note.getTitle() + " (copia)"));
            payload.put("content", new JSONString(note.getContent()));
            JSONArray tagsArray = new JSONArray();
            String[] noteTags = note.getTags() != null ? note.getTags() : new String[0];
            for (int i = 0; i < noteTags.length; i++) {
                tagsArray.set(i, new JSONString(noteTags[i]));
            }
            payload.put("tags", tagsArray);

            RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, GWT.getHostPageBaseURL() + "api/notes");
            builder.setHeader("Content-Type", "application/json");
            try {
                builder.sendRequest(payload.toString(), new RequestCallback() {
                    @Override
                    public void onResponseReceived(Request request, Response response) {
                        if (response.getStatusCode() == Response.SC_OK) {
                            feedbackLabel.setText("Nota duplicata!");
                        } else {
                            feedbackLabel.setText("Duplicazione fallita: " + response.getText());
                        }
                    }

                    @Override
                    public void onError(Request request, Throwable exception) {
                        feedbackLabel.setText("Errore durante la duplicazione: " + exception.getMessage());
                    }
                });
            } catch (RequestException e) {
                feedbackLabel.setText("Request error: " + e.getMessage());
            }
        });
    }
}