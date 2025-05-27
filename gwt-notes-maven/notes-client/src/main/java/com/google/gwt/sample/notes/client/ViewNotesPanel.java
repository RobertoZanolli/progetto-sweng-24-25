package com.google.gwt.sample.notes.client;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.sample.notes.shared.Note;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ViewNotesPanel extends Composite {

    private VerticalPanel panel = new VerticalPanel();
    private List<Note> notes = new ArrayList<>();
    private List<Note> filteredNotes = new ArrayList<>();
    private final Label feedbackLabel = new Label();
    private TextBox searchBox = new TextBox();
    private Button createNoteButton = new Button("Nuova Nota");
    private Button createTagButton = new Button("Nuovo Tag");
    
    @SuppressWarnings("deprecation")
    private ListBox tagListBox = new ListBox(true);

    private Button searchButton = new Button("Filtra");
    private Button exitButton = new Button("Esci");

    public ViewNotesPanel() {
        initWidget(panel);
        setupUI();
    }

    private void setupUI() {
        // Sezione di ricerca per parole chiave
        HorizontalPanel searchPanel = new HorizontalPanel();
        searchPanel.setSpacing(10);
        searchBox.getElement().setPropertyString("placeholder", "Cerca...");
        searchPanel.add(new Label("Cerca per parole chiave: "));
        searchPanel.add(searchBox);
        panel.add(searchPanel);

        // Sezione di ricerca per tag
        HorizontalPanel tagPanel = new HorizontalPanel();
        tagPanel.setSpacing(10);
        tagPanel.add(new Label("Cerca per tag (Ctrl+Click per selezione multipla): "));
        getTags();
        tagPanel.add(tagListBox);
        panel.add(tagPanel);

        getNotes();

        // Bottone per la navigazione
        HorizontalPanel buttonPanel = new HorizontalPanel();
        buttonPanel.setSpacing(10);
        buttonPanel.add(searchButton);
        buttonPanel.add(createNoteButton);
        buttonPanel.add(createTagButton);
        buttonPanel.add(exitButton);
        panel.add(feedbackLabel);
        panel.add(buttonPanel);


        // Gestione evento ricerca (filtra lista note)
        searchBox.addKeyUpHandler(event -> {
            String filter = searchBox.getText().toLowerCase();
            filteredNotes = notes.stream()
                .filter(n -> n.getTitle().toLowerCase().contains(filter) ||
                             n.getContent().toLowerCase().contains(filter))
                .collect(Collectors.toList());
            renderNotes();
        });
        // Oppure con bottone
        searchButton.addClickHandler(event -> {
            // ToDo
        });

        // Bottone per creare nuova nota (qui puoi collegare la logica di navigazione)
        createNoteButton.addClickHandler(event -> {
            // Ad esempio, puoi rimuovere questa view e aggiungere CreateNotePanel
            panel.clear();
            panel.add(new CreateNotePanel());
        });

        createTagButton.addClickHandler(event -> {
            // Simile al bottone sopra, cambia pannello o mostra dialog
            panel.clear();
            panel.add(new CreateTagPanel());
        });

        exitButton.addClickHandler(event -> {
            panel.clear();
            panel.add(new HomePanel());
            /*
             * ToDo: eseguire il logout (eliminare dati di sessione)
             */
        });
    }

    // Mostra correttamente le note
    private void renderNotes() {
        // Rimuovo precedenti note, ma mantengo barra di ricerca e bottoni
        if (panel.getWidgetCount() > 4) {
            // Tieni solo i primi 4 widget (searchPanel, tagPanel, buttonPanel e feedbackLabel)
            while (panel.getWidgetCount() > 4) {
                panel.remove(4);
            }
        }

        // Aggiungo note filtrate
        for (Note note : filteredNotes) {
            HorizontalPanel notePanel = new HorizontalPanel();
            Label noteLabel = new Label(note.getTitle() + " by " + note.getOwner().getEmail());
            Label createdAtLabel = new Label(note.getCreatedDate().toString());
            Button noteDetailButton = new Button("Vedi nota");
            notePanel.add(noteLabel);
            notePanel.add(createdAtLabel);
            notePanel.add(noteDetailButton);
            panel.add(notePanel);
        }
    }

    // Aggiunge le note alla lista
    public void getNotes() {
        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET,
                GWT.getHostPageBaseURL() + "api/notes");
        builder.setHeader("Content-Type", "application/json");
        try {
            builder.setCallback(new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {
                    if (response.getStatusCode() == Response.SC_OK) {
                        String json = response.getText();
                        notes = parseNotesJson(json);
                        filteredNotes = new ArrayList<>(notes);
                        renderNotes();
                    } else {
                        feedbackLabel.setText("Error fetching notes: " + response.getText());
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

    private List<Note> parseNotesJson(String json) {
        List<Note> result = new ArrayList<>();
        JSONValue value = JSONParser.parseStrict(json);
        JSONArray array = value.isArray();
        if (array != null) {
            for (int i = 0; i < array.size(); i++) {
                JSONValue obj = array.get(i);
                JSONObject noteObj = obj.isObject();
                if (noteObj != null) {
                    Note note = new Note();
                    JSONValue titleValue = noteObj.get("title");
                    if (titleValue != null && titleValue.isString() != null) {
                        note.setTitle(titleValue.isString().stringValue());
                    }
                    JSONValue contentValue = noteObj.get("content");
                    if (contentValue != null && contentValue.isString() != null) {
                        note.setContent(contentValue.isString().stringValue());
                    }
                    JSONValue tagsValue = noteObj.get("tags");
                    if (tagsValue != null && tagsValue.isArray() != null) {
                        JSONArray tagsArray = tagsValue.isArray();
                        String[] tags = new String[tagsArray.size()];
                        for (int j = 0; j < tagsArray.size(); j++) {
                            tags[j] = tagsArray.get(j).isString().stringValue();
                        }
                        note.setTags(tags);
                    }
                    result.add(note);
                }
            }
        }
        return result;
    }

    // Aggiunge i tag alla list box
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
                        feedbackLabel.setText("Error fetching tags: " + response.getText());
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
}