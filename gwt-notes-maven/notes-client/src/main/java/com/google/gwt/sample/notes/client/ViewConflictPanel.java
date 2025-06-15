package com.google.gwt.sample.notes.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.sample.notes.shared.ConcreteNote;
import com.google.gwt.sample.notes.shared.ConcreteVersion;
import com.google.gwt.sample.notes.shared.Note;
import com.google.gwt.sample.notes.shared.Permission;
import com.google.gwt.sample.notes.shared.Version;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.ui.RootPanel;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ViewConflictPanel extends Composite {

    private final VerticalPanel mainPanel = new VerticalPanel();
    private final HorizontalPanel contentPanel = new HorizontalPanel();
    private final TextArea originalTextContent = new TextArea();
    private final TextArea updatedTextContent = new TextArea();
    private final TextBox originalTextTitle = new TextBox();
    private final TextBox updatedTextTitle = new TextBox();
    private final ListBox tagListBoxOriginal = new ListBox();
    private final Button addTagButtonOriginal = new Button("Aggiungi tag");
    private final TextBox newTagBoxOriginal = new TextBox();
    private final ListBox tagListBoxUpdated = new ListBox();
    private final Button addTagButtonUpdated = new Button("Aggiungi tag");
    private final TextBox newTagBoxUpdated = new TextBox();
    private final Button acceptOriginalButton = new Button("Accetta Versione Originale");
    private final Button acceptUpdatedButton = new Button("Accetta Versione Aggiornata");
    private final ListBox permissionListBoxOriginal = new ListBox();
    private final ListBox permissionListBoxUpdated = new ListBox();
    private final VerticalPanel originalPanel = new VerticalPanel();
    private final VerticalPanel updatedPanel = new VerticalPanel();
    private final String tagLogName = "Tag";
    private final Button backButton = new Button("Indietro");
    private final Label feedbackLabel = new Label();
    private final Note originalNote;
    private final JSONObject updatedVersion;
    private final Map<String, String> permissionMap = new LinkedHashMap<String, String>() {
        {
            put("Privata", "PRIVATE");
            put("Lettura Pubblica", "READ");
            put("Scrittura Pubblica", "WRITE");
        }
    };;

    public ViewConflictPanel(Note originalNote, JSONObject updatedVersion) {
        this.originalNote = originalNote;
        this.updatedVersion = updatedVersion;
        initWidget(mainPanel);
        buildUI();
        setupHandlers();
    }

    private void buildUI() {
        // setUpName();

        mainPanel.setSpacing(10);
        tagListBoxOriginal.setMultipleSelect(true);
        tagListBoxUpdated.setMultipleSelect(true);

        Label title = new Label("Conflitto tra versioni");
        title.setStyleName("gwt-Label-title");
        mainPanel.add(title);

        originalTextTitle.setText(originalNote.getCurrentVersion().getTitle());
        updatedTextTitle.setText(updatedVersion.get("title").isString().stringValue());

        originalTextContent.setText(originalNote.getCurrentVersion().getContent());
        updatedTextContent.setText(updatedVersion.get("content").isString().stringValue());

        /*
         * originalTextContent.setReadOnly(true);
         * updatedTextContext.setReadOnly(true);
         */
        originalTextContent.setVisibleLines(15);
        updatedTextContent.setVisibleLines(15);

        originalTextContent.setCharacterWidth(40);
        updatedTextContent.setCharacterWidth(40);

        // disabilito i permessi per evitare modifiche
        permissionListBoxOriginal.setEnabled(false);
        permissionListBoxUpdated.setEnabled(false);

        // Aggiungi i permessi alla ListBox
        permissionListBoxOriginal.clear();
        permissionListBoxUpdated.clear();
        for (String permission : permissionMap.keySet()) {
            permissionListBoxOriginal.addItem(permission);
            permissionListBoxUpdated.addItem(permission);
        }

        // Imposta il permesso originale
        permissionListBoxOriginal.setSelectedIndex(originalNote.getPermission().ordinal());
        permissionListBoxUpdated.setSelectedIndex(originalNote.getPermission().ordinal());

        // verifico se l'utente è il proprietario della nota e nel caso abilito la
        // modifica dei permessi
        if (originalNote.isOwner(Session.getInstance().getUserEmail())) {
            permissionListBoxUpdated.setSelectedIndex((int) updatedVersion.get("permission").isNumber().doubleValue());
            permissionListBoxUpdated.setEnabled(true);
            permissionListBoxOriginal.setEnabled(true);
        }

        getTags();

        contentPanel.setSpacing(10);

        originalPanel.setSpacing(10);

        originalPanel.add(new Label("Versione Originale"));
        originalPanel.add(new Label("Titolo:"));
        originalPanel.add(originalTextTitle);
        originalPanel.add(new Label("Contenuto:"));
        originalPanel.add(originalTextContent);
        originalPanel.add(new Label("Tags:"));
        originalPanel.add(tagListBoxOriginal);
        originalPanel.add(new Label("Aggiungi nuovo tag:"));
        originalPanel.add(newTagBoxOriginal);
        originalPanel.add(addTagButtonOriginal);
        originalPanel.add(new Label("Permessi:"));
        originalPanel.add(permissionListBoxOriginal);
        originalPanel.add(acceptOriginalButton);

        updatedPanel.setSpacing(10);
        updatedPanel.add(new Label("Versione Aggiornata"));
        updatedPanel.add(new Label("Titolo:"));
        updatedPanel.add(updatedTextTitle);
        updatedPanel.add(new Label("Contenuto:"));
        updatedPanel.add(updatedTextContent);
        updatedPanel.add(new Label("Tags:"));
        updatedPanel.add(tagListBoxUpdated);
        updatedPanel.add(new Label("Aggiungi nuovo tag:"));
        updatedPanel.add(newTagBoxUpdated);
        updatedPanel.add(addTagButtonUpdated);

        updatedPanel.add(new Label("Permessi:"));
        updatedPanel.add(permissionListBoxUpdated);
        updatedPanel.add(acceptUpdatedButton);

        contentPanel.add(updatedPanel);
        contentPanel.add(originalPanel);
        mainPanel.add(contentPanel);
        mainPanel.add(feedbackLabel);

        mainPanel.setSpacing(10);
        mainPanel.add(backButton);
    }

    private void setupHandlers() {

        addTagButtonOriginal.addClickHandler(event -> {
            addTag(tagListBoxOriginal, newTagBoxOriginal);
        });

        addTagButtonUpdated.addClickHandler(event -> {
            addTag(tagListBoxUpdated, newTagBoxUpdated);
        });

        acceptOriginalButton.addClickHandler(event -> {
            updateNoteOriginal();
        });

        acceptUpdatedButton.addClickHandler(event -> {
            updateNoteUpdated();
        });

        backButton.addClickHandler(event -> {
            RootPanel.get("mainPanel").clear();
            RootPanel.get("mainPanel").add(new NoteDetailPanel(originalNote));
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
                        setUpTagListBoxes(response.getText());
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


    private void setUpTagListBoxes(String json) {
        List<String> tags = JsonParserUtil.parseTagsJson(json);

        // clear existing items
        tagListBoxOriginal.clear();
        tagListBoxUpdated.clear();
        newTagBoxOriginal.setText("");
        newTagBoxUpdated.setText("");

        // Add tags to both list boxes
        for (String tag : tags) {
            tagListBoxOriginal.addItem(tag);
            tagListBoxUpdated.addItem(tag);
        }

        // Set the original tags for the original note
        setSelectedTags(tagListBoxOriginal, originalNote.getTags());

        List<String> updatedTagsList = JsonParserUtil.parseTagsJson(updatedVersion.get("tags").toString());
        String[] updatedTags = updatedTagsList.toArray(new String[0]);

        // Set the updated tags for the updated version
        setSelectedTags(tagListBoxUpdated, updatedTags);
    }

    private void setSelectedTags(ListBox listBox, String[] tags) {
        for (String tag : tags) {
            int index = listBox.getItemCount();
            for (int i = 0; i < index; i++) {
                if (listBox.getItemText(i).equals(tag)) {
                    listBox.setItemSelected(i, true);
                    break;
                }
            }
        }
    }

    private void addTag(ListBox tagListBox, TextBox newTagBox) {
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
                        feedbackLabel.setText("Error: " + exception.getMessage());
                    }
                });
            } catch (RequestException e) {
                feedbackLabel.setText("Request error: " + e.getMessage());
            }
        } else {
            Window.alert("Inserisci un nome per il tag.");
        }
    }

    private void updateTagList(Boolean exists, String newTag) {
        if (!exists) {
            // Usiamo l'indice come valore, ma si può usare anche un UUID o il testo stesso
            tagListBoxOriginal.addItem(newTag, String.valueOf(tagListBoxOriginal.getItemCount() + 1));
            newTagBoxOriginal.setText("");

            tagListBoxUpdated.addItem(newTag, String.valueOf(tagListBoxUpdated.getItemCount() + 1));
            newTagBoxUpdated.setText("");
        } else {
            feedbackLabel.setText("Tag già presente.");
        }
    }

    private void updateNoteOriginal() {
        String title = originalTextTitle.getText().trim();
        String content = originalTextContent.getText().trim();

        if (title.isEmpty() || content.isEmpty()) {
            Window.alert("Titolo e contenuto sono obbligatori.");
            return;
        }

        // payload per la nuova versione
        JSONObject payload = new JSONObject();
        payload.put("title", new JSONString(title));
        payload.put("content", new JSONString(content));

        String selectedLabel = permissionListBoxOriginal.getItemText(permissionListBoxOriginal.getSelectedIndex());
        String selectedPermission = permissionMap.get(selectedLabel);
        payload.put("permission", new JSONString(selectedPermission));

        JSONArray tagsArray = new JSONArray();
        int tagIndex = 0;
        for (int i = 0; i < tagListBoxOriginal.getItemCount(); i++) {
            if (tagListBoxOriginal.isItemSelected(i)) {
                String tagValue = tagListBoxOriginal.getItemText(i);
                if (tagValue != null && !tagValue.trim().isEmpty()) {
                    tagsArray.set(tagIndex++, new JSONString(tagValue.trim()));
                }
            }
        }
        payload.put("tags", tagsArray);
        payload.put("lastKnownVersion", new JSONString(String.valueOf(originalNote.currentVersionNumber())));


        String url = GWT.getHostPageBaseURL() + "api/notes?id=" + originalNote.getId();
        RequestBuilder builder = new RequestBuilder(RequestBuilder.PUT, url);
        builder.setHeader("Content-Type", "application/json");
        builder.setIncludeCredentials(true);
        try {
            builder.sendRequest(payload.toString(), new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {
                    if (response.getStatusCode() == Response.SC_OK) {
                        feedbackLabel.setText("Nota modificata!");
                        RootPanel.get("mainPanel").clear();
                        RootPanel.get("mainPanel").add(new ViewNotesPanel());
                    } else if (response.getStatusCode() == Response.SC_CONFLICT) {
                        feedbackLabel.setText("Conflitto di versione. Quale vuoi mantenere?.");

                        // qui va inserita la getNote per mostrare il conflitto
                        getNoteById(originalNote.getId(), payload);
                    } else {
                        feedbackLabel.setText("Errore durante la modifica (" + response.getStatusCode() + "): " +
                                response.getStatusText() + " - " + response.getText());
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

    private void updateNoteUpdated() {
        String title = updatedTextTitle.getText().trim();
        String content = updatedTextContent.getText().trim();

        if (title.isEmpty() || content.isEmpty()) {
            Window.alert("Titolo e contenuto sono obbligatori.");
            return;
        }

        // payload per la nuova versione
        JSONObject payload = new JSONObject();
        payload.put("title", new JSONString(title));
        payload.put("content", new JSONString(content));

        String selectedLabel = permissionListBoxOriginal.getItemText(permissionListBoxOriginal.getSelectedIndex());
        String selectedPermission = permissionMap.get(selectedLabel);
        payload.put("permission", new JSONString(selectedPermission));

        JSONArray tagsArray = new JSONArray();
        int tagIndex = 0;
        for (int i = 0; i < tagListBoxUpdated.getItemCount(); i++) {
            if (tagListBoxUpdated.isItemSelected(i)) {
                String tagValue = tagListBoxUpdated.getItemText(i);
                if (tagValue != null && !tagValue.trim().isEmpty()) {
                    tagsArray.set(tagIndex++, new JSONString(tagValue.trim()));
                }
            }
        }
        payload.put("tags", tagsArray);
        payload.put("lastKnownVersion", new JSONString(String.valueOf(originalNote.currentVersionNumber())));


        String url = GWT.getHostPageBaseURL() + "api/notes?id=" + originalNote.getId();
        RequestBuilder builder = new RequestBuilder(RequestBuilder.PUT, url);
        builder.setHeader("Content-Type", "application/json");
        builder.setIncludeCredentials(true);
        try {
            builder.sendRequest(payload.toString(), new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {
                    if (response.getStatusCode() == Response.SC_OK) {
                        feedbackLabel.setText("Nota modificata!");
                        RootPanel.get("mainPanel").clear();
                        RootPanel.get("mainPanel").add(new ViewNotesPanel());
                    } else if (response.getStatusCode() == Response.SC_CONFLICT) {
                        feedbackLabel.setText("Conflitto di versione. Quale vuoi mantenere?.");

                        // qui va inserita la getNote per mostrare il conflitto
                        getNoteById(originalNote.getId(), payload);

                    } else {
                        feedbackLabel.setText("Errore durante la modifica (" + response.getStatusCode() + "): " +
                                response.getStatusText() + " - " + response.getText());
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
                            RootPanel.get("mainPanel").clear();
                            RootPanel.get("mainPanel").add(new ViewConflictPanel(note, payload));
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
