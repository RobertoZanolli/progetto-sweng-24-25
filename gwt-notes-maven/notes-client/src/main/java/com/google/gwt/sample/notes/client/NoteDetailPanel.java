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
import com.google.gwt.sample.notes.shared.Permission;
import com.google.gwt.sample.notes.shared.Version;
import com.google.gwt.sample.notes.shared.ConcreteNote;
import com.google.gwt.sample.notes.shared.ConcreteVersion;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.json.client.JSONArray;
import java.util.Date;
import com.google.gwt.user.client.Window;
import com.google.gwt.i18n.client.DateTimeFormat;

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
    private final Button viewHistory = new Button("Vedi cronologia");
    private final Button backButton = new Button("Indietro");
    private final Button hideButton = new Button("Non visualizzare più");
    private final ListBox permissionListBox = new ListBox();
    private boolean isEditMode = false;
    private final DateTimeFormat dateFormat = DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ss");
    private String email = Session.getInstance().getUserEmail();

    public NoteDetailPanel(Note note) {
        this.note = note;
        initWidget(panel);
        buildUI();
        setupHandlers();
    }

    private void buildUI() {
        panel.setSpacing(10);
        panel.add(new Label("Proprietario: " + note.getOwnerEmail()));

        panel.add(new Label("Titolo:"));
        titleBox.setText(note.getCurrentVersion().getTitle() != null ? note.getCurrentVersion().getTitle() : "N/A");
        titleBox.setEnabled(false); // Inizialmente non modificabile
        panel.add(titleBox);

        panel.add(new Label("Contenuto:"));
        contentBox
                .setText(note.getCurrentVersion().getContent() != null ? note.getCurrentVersion().getContent() : "N/A");
        contentBox.setVisibleLines(5);
        contentBox.setCharacterWidth(40);
        contentBox.setEnabled(false); // Inizialmente non modificabile
        panel.add(contentBox);

        panel.add(new Label("Permessi:"));
        permissionListBox.addItem("Privata", "PRIVATE");
        permissionListBox.addItem("Lettura Pubblica", "READ");
        permissionListBox.addItem("Scrittura Pubblica", "WRITE");
        permissionListBox.setSelectedIndex(note.getPermission().ordinal());
        permissionListBox.setEnabled(false); // Inizialmente non modificabile
        panel.add(permissionListBox);

        String tags = note.getTags() != null && note.getTags().length > 0
                ? String.join(", ", note.getTags())
                : "Nessun tag";
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
                ? note.getCreatedAt().toString()
                : "Data non disponibile"));
        panel.add(createdDateLabel);

        // Questa è la data dell'ultima versione in realtà
        lastModifiedDateLabel.setText("Ultima modifica: " + (note.getCurrentVersion().getUpdatedAt() != null
                ? note.getCurrentVersion().getUpdatedAt().toString()
                : "Data non disponibile"));
        panel.add(lastModifiedDateLabel);

        HorizontalPanel buttonPanel = new HorizontalPanel();
        buttonPanel.setSpacing(10);
        buttonPanel.add(deleteButton);
        buttonPanel.add(editButton);
        buttonPanel.add(duplicateButton);
        buttonPanel.add(viewHistory);
        buttonPanel.add(hideButton);
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
                builder.setIncludeCredentials(true);
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
                            feedbackLabel.setText("Errore: " + exception.getMessage());
                        }
                    });
                } catch (RequestException e) {
                    feedbackLabel.setText("Errore nella richiesta: " + e.getMessage());
                }
            } else {
                Window.alert("Inserisci un nome per il tag.");
            }
        });

        deleteButton.addClickHandler(event -> {
            if (note.getPermission().canEdit(email, note)) {

                String noteId = note.getId();

                String url = GWT.getHostPageBaseURL() + "api/notes?id=" + noteId;
                RequestBuilder builder = new RequestBuilder(RequestBuilder.DELETE, url);
                builder.setHeader("Content-Type", "application/json");
                builder.setIncludeCredentials(true);
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
                    feedbackLabel.setText("Errore nella richiesta: " + e.getMessage());
                }
            } else {
                Window.alert("Non hai i permessi necessari per eliminare questa nota.");
            }
        });

        editButton.addClickHandler(event -> {
            if (!isEditMode) {
                if (note.getPermission().canEdit(email, note)) {
                    isEditMode = true;
                    titleBox.setEnabled(true);
                    contentBox.setEnabled(true);
                    tagListBox.setEnabled(true);
                    newTagBox.setEnabled(true);
                    addTagButton.setEnabled(true);

                    if (note.isOwner(Session.getInstance().getUserEmail())) {
                        permissionListBox.setEnabled(true);
                    }
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
                    Window.alert("Non hai i permessi necessari per modificare questa nota.");
                }
            } else {

                String title = titleBox.getText().trim();
                String content = contentBox.getText().trim();

                if (title.isEmpty() || content.isEmpty()) {
                    Window.alert("Titolo e contenuto sono obbligatori.");
                    return;
                }

                // payload per la nuova versione
                JSONObject payload = new JSONObject();
                payload.put("title", new JSONString(title));
                payload.put("content", new JSONString(content));

                String selectedPermission = permissionListBox.getValue(permissionListBox.getSelectedIndex());
                payload.put("permission", new JSONString(selectedPermission));

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
                DateTimeFormat fmt = DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ss");
                payload.put("lastKnownUpdate", new JSONString(fmt.format(note.getCurrentVersion().getUpdatedAt())));


                String url = GWT.getHostPageBaseURL() + "api/notes?id=" + note.getId();
                RequestBuilder builder = new RequestBuilder(RequestBuilder.PUT, url);
                builder.setHeader("Content-Type", "application/json");
                builder.setIncludeCredentials(true);
                try {
                    builder.sendRequest(payload.toString(), new RequestCallback() {
                        @Override
                        public void onResponseReceived(Request request, Response response) {
                            if (response.getStatusCode() == Response.SC_OK) {
                                feedbackLabel.setText("Nota modificata con successo!");

                                Version newVersion = new ConcreteVersion();
                                newVersion.setTitle(title);
                                newVersion.setContent(content);
                                newVersion.setUpdatedAt(new Date());
                                note.newVersion(newVersion);

                                List<String> selectedTags = new ArrayList<>();
                                for (int i = 0; i < tagListBox.getItemCount(); i++) {
                                    if (tagListBox.isItemSelected(i)) {
                                        selectedTags.add(tagListBox.getValue(i));
                                    }
                                }
                                String[] tagsArray = selectedTags.toArray(new String[0]);
                                note.setTags(tagsArray);
                                String tagsString = tagsArray.length > 0 ? String.join(", ", tagsArray) : "Nessun tag";
                                tagsLabel.setText("Tag: " + tagsString);

                                titleBox.setText(newVersion.getTitle());
                                contentBox.setText(newVersion.getContent());
                                lastModifiedDateLabel
                                        .setText("Ultima modifica: " + newVersion.getUpdatedAt().toString());

                                isEditMode = false;
                                titleBox.setEnabled(false);
                                contentBox.setEnabled(false);
                                tagListBox.setEnabled(false);
                                newTagBox.setEnabled(false);
                                addTagButton.setEnabled(false);
                                permissionListBox.setEnabled(false);
                                editButton.setText("Modifica");
                            } else if (response.getStatusCode() == Response.SC_CONFLICT) {
                                feedbackLabel.setText("Conflitto di versione. Quale vuoi mantenere?.");

                                // ottengo l'ultima versione della nota
                                getNoteById(note.getId(), payload);
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

            if (note.getPermission().canEdit(email, note)) {
                JSONObject payload = new JSONObject();

                // Duplico tutte le versioni
                JSONArray versionsArray = new JSONArray();
                for (int i = 0; i < note.getAllVersions().size(); i++) {
                    Version v = note.getAllVersions().get(i);
                    JSONObject vObj = new JSONObject();
                    vObj.put("title", new JSONString(
                            (i == note.getAllVersions().size() - 1) ? v.getTitle() + " (copia)" : v.getTitle()));
                    vObj.put("content", new JSONString(v.getContent()));
                    if (v.getUpdatedAt() != null) {
                        vObj.put("updatedAt", new JSONString(dateFormat.format(v.getUpdatedAt())));
                    }
                    versionsArray.set(i, vObj);
                }
                payload.put("versions", versionsArray);
                JSONArray tagsArray = new JSONArray();
                String[] noteTags = note.getTags() != null ? note.getTags() : new String[0];
                for (int i = 0; i < noteTags.length; i++) {
                    tagsArray.set(i, new JSONString(noteTags[i]));
                }
                payload.put("tags", tagsArray);
                payload.put("ownerEmail", new JSONString(note.getOwnerEmail()));

                String selectedPermission = permissionListBox.getValue(permissionListBox.getSelectedIndex());
                payload.put("permission", new JSONString(selectedPermission));

                RequestBuilder builder = new RequestBuilder(RequestBuilder.POST,
                        GWT.getHostPageBaseURL() + "api/notes");
                builder.setHeader("Content-Type", "application/json");
                builder.setIncludeCredentials(true);
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
                    feedbackLabel.setText("Errore nella richiesta: " + e.getMessage());
                }
            } else {
                Window.alert("Non hai i permessi necessari per duplicare questa nota.");
                return;
            }
        });

        viewHistory.addClickHandler(event -> {
            panel.clear();
            panel.add(new NoteHistoryPanel(note));
        });

        hideButton.addClickHandler(event -> {
            if (note.isOwner(email)) {
                Window.alert("Il proprietario della nota non può rimuoversi dalla visualizzazione della nota.");
                return;
            }

            String hideUrl = GWT.getHostPageBaseURL() + "api/notes/hide?id=" + note.getId();
            RequestBuilder hideBuilder = new RequestBuilder(RequestBuilder.PUT, hideUrl);
            hideBuilder.setHeader("Content-Type", "text/plain");
            hideBuilder.setIncludeCredentials(true);
            try {
                hideBuilder.sendRequest("true", new RequestCallback() {
                    @Override
                    public void onResponseReceived(Request request, Response response) {
                        if (response.getStatusCode() == Response.SC_OK) {
                            feedbackLabel.setText("Nota rimossa dalla tua vista.");
                            panel.clear();
                            panel.add(new ViewNotesPanel());
                        } else {
                            feedbackLabel.setText("Errore: " + response.getStatusText());
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
        });
    }

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
                        feedbackLabel.setText("Errore nel caricamento di " + tagLogName + ": " + response.getText());
                    }
                }

                @Override
                public void onError(Request request, Throwable exception) {
                    feedbackLabel.setText("Errore: " + exception.getMessage());
                }
            });
            builder.send();
        } catch (RequestException e) {
            feedbackLabel.setText("Errore nella richiesta: " + e.getMessage());
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
            feedbackLabel.setText("Tag già presente");
        }
    }

    public void getNoteById(String noteId, JSONObject payload) {
        if (noteId == null || noteId.trim().isEmpty()) {
            feedbackLabel.setText("ID nota non valido.");
            return;
        }

        String url = GWT.getHostPageBaseURL() + "api/notes?id=" + URL.encodeQueryString(noteId);
        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);
        builder.setHeader("Content-Type", "application/json");
        builder.setIncludeCredentials(true);

        try {
            builder.sendRequest(null, new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {
                    if (response.getStatusCode() == Response.SC_OK) {
                        String json = response.getText();
                        Note note = parseSingleNoteJson(json);
                        if (note == null) {
                            feedbackLabel.setText("Nota non trovata o malformata.");
                        } else {
                            panel.clear();
                            panel.add(new ViewConflictPanel(note, payload));
                        }
                    } else {
                        feedbackLabel.setText("Errore nel recupero della nota: " + response.getStatusText());
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

    private Note parseSingleNoteJson(String json) {
        JSONValue value = JSONParser.parseStrict(json);
        JSONObject obj = value.isObject();

        if (obj == null) {
            feedbackLabel.setText("Errore: risposta JSON non valida");
            return null;
        }

        DateTimeFormat dateFormat = DateTimeFormat.getFormat("MMM d, yyyy, h:mm:ss a");

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
        return note;
    }
}