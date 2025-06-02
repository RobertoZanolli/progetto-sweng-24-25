package com.google.gwt.sample.notes.client;


import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.sample.notes.shared.Note;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.json.client.JSONArray;
import java.util.Date;
import com.google.gwt.user.client.Window;

public class NoteDetailPanel extends Composite {

    private final VerticalPanel panel = new VerticalPanel();
    private final Note note; // Nota da visualizzare/modificare
    private final Label feedbackLabel = new Label();
    private final TextBox titleBox = new TextBox();
    private final TextArea contentBox = new TextArea();
    private final Label tagsLabel = new Label();
    private final Label createdDateLabel = new Label();
    private final Label lastModifiedDateLabel = new Label();
    @SuppressWarnings("deprecation")
    private final ListBox tagListBox = new ListBox(true);
    private final Button addTagButton = new Button("Aggiungi tag");
    private final TextBox newTagBox = new TextBox();
    private final String tagLogName = "Tag";
    private final Button deleteButton = new Button("Elimina");
    private final Button editButton = new Button("Modifica");
    private final Button duplicateButton = new Button("Duplica");
    private final Button backButton = new Button("Indietro");
    private boolean isEditMode = false;


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

        panel.add(new Label("Tag (usa 'Ctrl+Click' per selezione multipla):"));

        getTags();
        panel.add(tagListBox);

        panel.add(new Label("Aggiungi nuovo tag:"));
        panel.add(newTagBox);
        panel.add(addTagButton);

        tagListBox.setEnabled(false);
        newTagBox.setEnabled(false);
        addTagButton.setEnabled(false);


        // CreatedDate
        createdDateLabel.setText("Creata: " + (note.getCreatedAt() != null 
            ? note.getCreatedAt().toString() : "Data non disponibile"));
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

        addTagButton.addClickHandler(event -> {
            String newTag = newTagBox.getText().trim();
            if (!newTag.isEmpty()) {
                JSONObject payload = new JSONObject();
                payload.put("name", new JSONString(newTag));

                RequestBuilder builder = new RequestBuilder(RequestBuilder.POST,
                        GWT.getHostPageBaseURL() + "api/tags");
                builder.setHeader("Content-Type", "application/json");
                try {
                    builder.sendRequest(payload.toString(), new RequestCallback() {
                        @Override
                        public void onResponseReceived(Request request, Response response) {
                            if (response.getStatusCode() == Response.SC_OK) {
                                feedbackLabel.setText(tagLogName + " created!");
                                updateTagList(false, newTag);
                            } else if (response.getStatusCode() == Response.SC_CONFLICT) {
                                feedbackLabel.setText(tagLogName + " already exists.");
                                updateTagList(true, newTag);
                            } else {
                                feedbackLabel.setText(tagLogName + " creation failed: " + response.getText());
                            }
                        }

                        @Override
                        public void onError(Request request, Throwable exception) {
                            feedbackLabel.setText("Error: " + exception.getMessage());
                        }
                    });
                } catch (RequestException e) {
                    feedbackLabel.setText("Request error: " + e.getMessage());
                }
            } else {
                Window.alert("Inserisci un nome per il tag.");
            }
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
            if (!isEditMode) {
                isEditMode = true;
                titleBox.setEnabled(true);
                contentBox.setEnabled(true);
                tagListBox.setEnabled(true);
                newTagBox.setEnabled(true);
                addTagButton.setEnabled(true);
                editButton.setText("Salva modifiche");

                String[] noteTags = note.getTags() != null ? note.getTags() : new String[0];
                for (int i = 0; i < tagListBox.getItemCount(); i++) {
                    String tag = tagListBox.getValue(i);
                    boolean isSelected = false;
                    for (String noteTag : noteTags) {
                        if (tag.equals(noteTag)) {
                            isSelected = true;
                            break;
                        }
                    }
                    tagListBox.setItemSelected(i, isSelected);
                }
            } else {
                // Salvo le modifiche
                String title = titleBox.getText().trim();
                String content = contentBox.getText().trim();

                if (title.isEmpty() || content.isEmpty()) {
                    Window.alert("Titolo e contenuto sono obbligatori.");
                    return;
                }

                JSONObject payload = new JSONObject();
                payload.put("id", new JSONString(note.getId()));
                payload.put("title", new JSONString(title));
                payload.put("content", new JSONString(content));

                JSONArray tagsArray = new JSONArray();
                int tagIndex = 0;

                for (int i = 0; i < tagListBox.getItemCount(); i++) {
                    if (tagListBox.isItemSelected(i)) {
                        String tagValue = tagListBox.getItemText(i);
                        if (tagValue != null && !tagValue.trim().isEmpty()) {
                            tagsArray.set(tagIndex++, new JSONString(tagValue.trim()));
                        }
                    }
                }
                payload.put("tags", tagsArray);

                if (note.getOwnerEmail() != null) {
                    JSONObject ownerObj = new JSONObject();
                    ownerObj.put("email", new JSONString(note.getOwnerEmail()));
                    payload.put("owner", ownerObj);
                }

                String url = GWT.getHostPageBaseURL() + "api/notes?id=" + note.getId();
                RequestBuilder builder = new RequestBuilder(RequestBuilder.PUT, url);
                builder.setHeader("Content-Type", "application/json");

                try {
                    builder.sendRequest(payload.toString(), new RequestCallback() {
                        @Override
                        public void onResponseReceived(Request request, Response response) {
                            if (response.getStatusCode() == Response.SC_OK) {
                                feedbackLabel.setText("Nota modificata con successo!");
                                note.setTitle(title);
                                note.setContent(content);
                                note.setLastModifiedDate(new Date());

                                    // Aggiorno i tag della nota con quelli selezionati
                                    List<String> selectedTags = new ArrayList<>();
                                    for (int i = 0; i < tagListBox.getItemCount(); i++) {
                                        if (tagListBox.isItemSelected(i)) {
                                            selectedTags.add(tagListBox.getValue(i));
                                        }
                                    }
                                    String[] tagsArray = selectedTags.toArray(new String[0]);
                                    note.setTags(tagsArray);

                                    // Aggiorno la label con i tag nuovi
                                    String tagsString = tagsArray.length > 0 ? String.join(", ", tagsArray) : "Nessun tag";
                                    tagsLabel.setText("Tag: " + tagsString);

                                isEditMode = false;
                                titleBox.setEnabled(false);
                                contentBox.setEnabled(false);
                                tagListBox.setEnabled(false);
                                newTagBox.setEnabled(false);
                                addTagButton.setEnabled(false);
                                
                                editButton.setText("Modifica");
                            } else {
                                feedbackLabel.setText("Errore durante la modifica: " + response.getStatusText());
                            }
                        }

                        @Override
                        public void onError(Request request, Throwable exception) {
                            feedbackLabel.setText("Errore: " + exception.getMessage());
                        }
                    });
                } catch (RequestException e) {
                    feedbackLabel.setText("Errore nella richiesta: " + e.getMessage());
                }
            }
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

    private void getTags() {
        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET,
                GWT.getHostPageBaseURL() + "api/tags");
        builder.setHeader("Content-Type", "application/json");
        try {
            builder.setCallback(new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {
                    if (response.getStatusCode() == Response.SC_OK) {
                        String json = response.getText();
                        List<String> tags = parseTagsJson(json);

                        for (String tag : tags) {
                            tagListBox.addItem(tag);
                        }
                    } else {
                        feedbackLabel.setText("Error fetching " + tagLogName + ": " + response.getText());
                    }
                }

                @Override
                public void onError(Request request, Throwable exception) {
                    feedbackLabel.setText("Error: " + exception.getMessage());
                }
            });
            builder.send();
        } catch (RequestException e) {
            feedbackLabel.setText("Request error: " + e.getMessage());
        }
    }

    public List<String> parseTagsJson(String jsonString) {
        List<String> result = new ArrayList<>();
        JSONValue value = JSONParser.parseStrict(jsonString);
        JSONArray array = value.isArray();
        if (array != null) {
            for (int i = 0; i < array.size(); i++) {
                JSONValue v = array.get(i);
                JSONString s = v.isString();
                if (s != null) {
                    result.add(s.stringValue());
                }
            }
        }
        return result;
    }

    private void updateTagList(Boolean exists, String newTag) {
        if (!exists) {
            tagListBox.addItem(newTag, newTag);
            newTagBox.setText("");
        } else {
            feedbackLabel.setText("Tag già presente.");
        }
    }
}