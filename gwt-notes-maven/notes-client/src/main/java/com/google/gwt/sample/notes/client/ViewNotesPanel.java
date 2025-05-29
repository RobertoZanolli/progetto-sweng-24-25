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
import com.google.gwt.sample.notes.shared.Note;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.i18n.client.DateTimeFormat;

public class ViewNotesPanel extends Composite {
    private final VerticalPanel panel = new VerticalPanel();
    private List<Note> notes = new ArrayList<>();
    private List<Note> filteredNotes = new ArrayList<>();
    private final Label feedbackLabel = new Label();
    private final TextBox searchBox = new TextBox();
    private final Button createNoteButton = new Button("Nuova Nota");
    private final Button createTagButton = new Button("Nuovo Tag");
    private final Button exitButton = new Button("Esci");
    
    @SuppressWarnings("deprecation")
    private final ListBox tagListBox = new ListBox(true);

    public ViewNotesPanel() {
        initWidget(panel);
        buildUI();
        setupHandlers();
    }

    private void buildUI() {
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
        tagPanel.add(new Label("Cerca per tag (usa 'Ctrl + Click' per selezione multipla): "));
        getTags();
        tagPanel.add(tagListBox);
        panel.add(tagPanel);

        getNotes();

        // Bottone per la navigazione
        HorizontalPanel buttonPanel = new HorizontalPanel();
        buttonPanel.setSpacing(10);
        buttonPanel.add(createNoteButton);
        buttonPanel.add(createTagButton);
        buttonPanel.add(exitButton);
        panel.add(feedbackLabel);
        panel.add(buttonPanel);
    }

    private void setupHandlers() {
        // Gestione evento ricerca (filtra lista note per parola chiave)
        searchBox.addKeyUpHandler(event -> {
            filterByKeyWord();
            renderNotes();
        });

        // Gestione evento selezione tag (filtra lista note per tag)
        tagListBox.addChangeHandler(event -> {
            filterByTag();
            renderNotes();
        });
        
        createNoteButton.addClickHandler(event -> {
            panel.clear();
            panel.add(new CreateNotePanel());
        });

        createTagButton.addClickHandler(event -> {
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

    private void filterByTag() {
        List<String> selectedTags = new ArrayList<>();
        for (int i = 0; i < tagListBox.getItemCount(); i++) {
            if (tagListBox.isItemSelected(i)) {
                selectedTags.add(tagListBox.getItemText(i).toLowerCase());
            }
        }

        filteredNotes = notes.stream()
            .filter(n -> {
                String[] noteTags = n.getTags() != null ? n.getTags() : new String[0];
                for (String tag : noteTags) {
                    if (selectedTags.contains(tag.toLowerCase())) {
                        return true;
                    }
                }
                return selectedTags.isEmpty();
            })
            .collect(Collectors.toList());
    }

    private void filterByKeyWord() {
        String keyWord = searchBox.getText().toLowerCase();
        filteredNotes = notes.stream()
            .filter(n -> {
                String title = n.getTitle() != null ? n.getTitle().toLowerCase() : "";
                String content = n.getContent() != null ? n.getContent().toLowerCase() : "";
                return title.contains(keyWord) || content.contains(keyWord);
            })
            .collect(Collectors.toList());
    }

    // Mostra le note filtrate
    private void renderNotes() {
        // Rimuovo precedenti note, ma mantengo barra di ricerca e bottoni
        if (panel.getWidgetCount() > 4) {
            while (panel.getWidgetCount() > 4) {
                panel.remove(4);
            }
        }

        // Aggiungo note filtrate
        for (Note note : filteredNotes) {
            VerticalPanel notePanel = new VerticalPanel();
            notePanel.setSpacing(5);

            Label titleLabel = new Label("Titolo: " + (note.getTitle() != null ? note.getTitle() : "N/A"));
            notePanel.add(titleLabel);

            Label contentLabel = new Label("Contenuto: " + (note.getContent() != null ? note.getContent() : "N/A"));
            notePanel.add(contentLabel);

            String tags = note.getTags() != null && note.getTags().length > 0 
                ? String.join(", ", note.getTags()) : "Nessun tag";
            Label tagsLabel = new Label("Tag: " + tags);
            notePanel.add(tagsLabel);

            // CreatedDate
            Label createdAtLabel = new Label("Creata: " + (note.getCreatedDate() != null ? note.getCreatedDate().toString() : "Data non disponibile"));
            notePanel.add(createdAtLabel);

            // LastModifiedDate
            Label lastModifiedLabel = new Label("Ultima modifica: " + (note.getLastModifiedDate() != null ? note.getLastModifiedDate().toString() : "Data non disponibile"));
            notePanel.add(lastModifiedLabel);

            // Bottone per i dettagli
            Button noteDetailButton = new Button("Vedi nota");
            noteDetailButton.addClickHandler(event -> {
                panel.clear();
                panel.add(new NoteDetailPanel(note));
            });
            notePanel.add(noteDetailButton);

            panel.add(notePanel);

            // Divisore tra le note
            HTMLPanel divider = new HTMLPanel("<hr>");
            panel.add(divider);
        }
    }

    // Aggiunge le note alla lista
    public void getNotes() {
        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET,
                GWT.getHostPageBaseURL() + "notes");
        builder.setHeader("Content-Type", "application/json");

        try {
            builder.sendRequest(null, new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {
                    if (response.getStatusCode() == Response.SC_OK) {
                        String json = response.getText();
                        notes = parseNotesJson(json);
                        filteredNotes = new ArrayList<>(notes);
                        renderNotes();
                    } else {
                        feedbackLabel.setText("Errore nel recupero delle note: " + response.getStatusText());
                    }
                }

                @Override
                public void onError(Request request, Throwable exception) {
                    feedbackLabel.setText("Errore durante la richiesta: " + exception.getMessage());
                }
            });
        } catch (RequestException e) {
            feedbackLabel.setText("Errore nella richiesta: " + e.getMessage());
        }
    }

    private List<Note> parseNotesJson(String json) {
        List<Note> result = new ArrayList<>();
        JSONValue value = JSONParser.parseStrict(json);
        JSONArray array = value.isArray();

        if (array == null) {
            feedbackLabel.setText("Errore: risposta JSON non valida");
            return result;
        }

        DateTimeFormat dateFormat = DateTimeFormat.getFormat("MMM d, yyyy, h:mm:ss a");

        for (int i = 0; i < array.size(); i++) {
            JSONValue noteVal = array.get(i);
            if (noteVal != null && noteVal.isObject() != null) {
                JSONObject obj = noteVal.isObject();
                Note note = new Note(obj.get("id").isString().stringValue());

                // Title
                if (obj.containsKey("title") && obj.get("title").isString() != null) {
                    note.setTitle(obj.get("title").isString().stringValue());
                }

                // Content
                if (obj.containsKey("content") && obj.get("content").isString() != null) {
                    note.setContent(obj.get("content").isString().stringValue());
                }

                // CreatedDate
                if (obj.containsKey("createdDate") && obj.get("createdDate").isString() != null) {
                    try {
                        String dateStr = obj.get("createdDate").isString().stringValue();
                        // Sostituisci non-breaking space con spazio normale
                        dateStr = dateStr.replace("\u202f", " ");
                        note.setCreatedDate(dateFormat.parse(dateStr));
                    } catch (IllegalArgumentException e) {
                        GWT.log("Errore parsing createdDate: " + e.getMessage() + " per la stringa: " + obj.get("createdDate").isString().stringValue());
                    }
                }

                // LastModifiedDate
                if (obj.containsKey("lastModifiedDate") && obj.get("lastModifiedDate").isString() != null) {
                    try {
                        String dateStr = obj.get("lastModifiedDate").isString().stringValue();
                        // Sostituisci non-breaking space con spazio normale
                        dateStr = dateStr.replace("\u202f", " ");
                        note.setLastModifiedDate(dateFormat.parse(dateStr));
                    } catch (IllegalArgumentException e) {
                        GWT.log("Errore parsing lastModifiedDate: " + e.getMessage() + " per la stringa: " + obj.get("lastModifiedDate").isString().stringValue());
                    }
                }

                // Tags
                if (obj.containsKey("tags") && obj.get("tags").isArray() != null) {
                    JSONArray tagsArray = obj.get("tags").isArray();
                    String[] tags = new String[tagsArray.size()];
                    for (int t = 0; t < tagsArray.size(); t++) {
                        if (tagsArray.get(t).isString() != null) {
                            tags[t] = tagsArray.get(t).isString().stringValue();
                        } else {
                            tags[t] = "";
                        }
                    }
                    note.setTags(tags);
                }

                
                result.add(note);
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