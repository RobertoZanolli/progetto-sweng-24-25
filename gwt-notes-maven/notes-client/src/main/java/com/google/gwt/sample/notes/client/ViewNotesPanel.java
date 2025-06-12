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
import com.google.gwt.sample.notes.shared.ConcreteNote;
import com.google.gwt.sample.notes.shared.Note;
import com.google.gwt.sample.notes.shared.Permission;
import com.google.gwt.sample.notes.shared.Version;
import com.google.gwt.sample.notes.shared.ConcreteVersion;
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
    private String currentKeyword = "";
    private List<String> currentSelectedTags = new ArrayList<>();
    private final Label feedbackLabel = new Label();
    private final TextBox searchBox = new TextBox();
    private final Button createNoteButton = new Button("Nuova Nota");
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
        
        panel.add(new Label("Utente loggato: " + Session.getInstance().getUserEmail()));


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
        buttonPanel.add(exitButton);
        panel.add(feedbackLabel);
        panel.add(buttonPanel);
    }

    private void setupHandlers() {
        // Gestione evento ricerca (filtra lista note per parola chiave)
        searchBox.addKeyUpHandler(event -> {
            currentKeyword = searchBox.getText().toLowerCase();
            applyFilters();
        });

        // Gestione evento selezione tag (filtra lista note per tag)
        tagListBox.addChangeHandler(event -> {
            currentSelectedTags.clear();
            for (int i = 0; i < tagListBox.getItemCount(); i++) {
                if (tagListBox.isItemSelected(i)) {
                    currentSelectedTags.add(tagListBox.getItemText(i).toLowerCase());
                }
            }
            applyFilters();
        });

        createNoteButton.addClickHandler(event -> {
            panel.clear();
            panel.add(new CreateNotePanel());
        });

        exitButton.addClickHandler(event -> {
            panel.clear();
            panel.add(new HomePanel());
        });
    }

    private void applyFilters() {
        filteredNotes = notes.stream()
                .filter(n -> {
                    // Filtro per keyword
                    String title = n.getCurrentVersion().getTitle() != null
                            ? n.getCurrentVersion().getTitle().toLowerCase()
                            : "";
                    String content = n.getCurrentVersion().getContent() != null
                            ? n.getCurrentVersion().getContent().toLowerCase()
                            : "";
                    boolean matchesKeyword = currentKeyword.isEmpty() || title.contains(currentKeyword)
                            || content.contains(currentKeyword);

                    // Filtro per tag
                    String[] noteTags = n.getTags() != null ? n.getTags() : new String[0];
                    boolean matchesTags = currentSelectedTags.isEmpty() ||
                            currentSelectedTags.stream().anyMatch(
                                    tag -> java.util.Arrays.stream(noteTags).anyMatch(t -> t.equalsIgnoreCase(tag)));

                    return matchesKeyword && matchesTags;
                })
                .collect(Collectors.toList());
        renderNotes();
    }

    // Mostra le note filtrate
    private void renderNotes() {
        // Rimuovo precedenti note, ma mantengo barra di ricerca e bottoni
        if (panel.getWidgetCount() > 5) {
            while (panel.getWidgetCount() > 5) {
                panel.remove(5);
            }
        }

        // Aggiungo note filtrate
        for (Note note : filteredNotes) {
            VerticalPanel notePanel = new VerticalPanel();
            notePanel.setSpacing(5);

            Label titleLabel = new Label("Titolo: "
                    + (note.getCurrentVersion().getTitle() != null ? note.getCurrentVersion().getTitle() : "N/A"));
            notePanel.add(titleLabel);

            String tags = note.getTags() != null && note.getTags().length > 0
                    ? String.join(", ", note.getTags())
                    : "Nessun tag";
            Label tagsLabel = new Label("Tag: " + tags);
            notePanel.add(tagsLabel);

            Label createdAtLabel = new Label("Creata: "
                    + (note.getCreatedAt() != null ? note.getCreatedAt().toString() : "Data non disponibile"));
            notePanel.add(createdAtLabel);

            Label updatedAtLabel = new Label("Ultima modifica: " + (note.getCurrentVersion().getUpdatedAt() != null
                    ? note.getCurrentVersion().getUpdatedAt().toString()
                    : "Data non disponibile"));
            notePanel.add(updatedAtLabel);

            Label ownerLabel= new Label("Proprietario: "+ note.getOwnerEmail());
            notePanel.add(ownerLabel);
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
                GWT.getHostPageBaseURL() + "api/notes");
        builder.setHeader("Content-Type", "application/json");
        builder.setIncludeCredentials(true);
        try {
            builder.sendRequest(null, new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {
                    System.out.println("Notes response: " + response.getText());
                    if (response.getStatusCode() == Response.SC_OK) {
                        String json = response.getText();
                        notes = parseNotesJson(json);
                        System.out.println("Parsed notes count: " + notes.size());
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
                Note note = new ConcreteNote();
                // ID
                if (obj.containsKey("id") && obj.get("id").isString() != null) {
                    note.setId(obj.get("id").isString().stringValue());
                }
                // OwnerEmail
                if (obj.containsKey("ownerEmail") && obj.get("ownerEmail").isString() != null) {
                    note.setOwnerEmail(obj.get("ownerEmail").isString().stringValue());
                }
                // CreatedDate
                if (obj.containsKey("createdAt") && obj.get("createdAt").isString() != null) {
                    try {
                        String dateStr = obj.get("createdAt").isString().stringValue();
                        dateStr = dateStr.replace("\u202f", " ");
                        note.setCreatedAt(dateFormat.parse(dateStr));
                    } catch (IllegalArgumentException e) {
                        GWT.log("Errore parsing createdAt: " + e.getMessage());
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
                // Permission
                if (obj.containsKey("permission") && obj.get("permission").isString() != null) {
                    note.setPermission(Permission.valueOf(obj.get("permission").isString().stringValue()));
                }
                // Versions
                if (obj.containsKey("versions") && obj.get("versions").isArray() != null) {
                    JSONArray versionsArray = obj.get("versions").isArray();
                    for (int v = 0; v < versionsArray.size(); v++) {
                        JSONObject versionObj = versionsArray.get(v).isObject();
                        if (versionObj != null) {
                            Version version = new ConcreteVersion();
                            // Title
                            if (versionObj.containsKey("title") && versionObj.get("title").isString() != null) {
                                version.setTitle(versionObj.get("title").isString().stringValue());
                            }
                            // Content
                            if (versionObj.containsKey("content") && versionObj.get("content").isString() != null) {
                                version.setContent(versionObj.get("content").isString().stringValue());
                            }
                            // UpdatedAt
                            if (versionObj.containsKey("updatedAt") && versionObj.get("updatedAt").isString() != null) {
                                try {
                                    String dateStr = versionObj.get("updatedAt").isString().stringValue();
                                    dateStr = dateStr.replace("\u202f", " ");
                                    version.setUpdatedAt(dateFormat.parse(dateStr));
                                } catch (IllegalArgumentException e) {
                                    GWT.log("Errore parsing updatedAt: " + e.getMessage());
                                }
                            }
                            note.newVersion(version);
                            System.out.println(
                                    "Parsed note: id=" + note.getId() + ", versions=" + note.getAllVersions().size());
                        }
                    }
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
        builder.setIncludeCredentials(true);
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