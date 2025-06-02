package com.google.gwt.sample.notes.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import java.util.ArrayList;
import java.util.List;

public class CreateNotePanel extends Composite {

    private final VerticalPanel panel = new VerticalPanel();
    private final TextBox titleBox = new TextBox();
    private final TextArea contentBox = new TextArea();

    @SuppressWarnings("deprecation")
    private final ListBox tagListBox = new ListBox(true); // selezione multipla
    
    private final Button saveButton = new Button("Salva nota");
    private final Label charCountLabel = new Label("0 / 280");
    private final Label feedbackLabel = new Label();
    private final TextBox newTagBox = new TextBox();
    private final Button addTagButton = new Button("Aggiungi tag");
    private final Button backButton = new Button("Indietro");
    private final String tagLogName = "Tag";
    private final String noteLogName = "Note";

    public CreateNotePanel() {
        initWidget(panel);
        buildUI();
        setupHandlers();
    }

    private void buildUI() {
        panel.setSpacing(10);

        panel.add(new Label("Titolo:"));
        panel.add(titleBox);

        panel.add(new Label("Contenuto (max 280 caratteri):"));
        contentBox.setVisibleLines(5);
        contentBox.setCharacterWidth(40);
        panel.add(contentBox);
        panel.add(charCountLabel);

        panel.add(new Label("Tag (usa 'Ctrl+Click' per selezione multipla):"));

        getTags();
        panel.add(tagListBox);

        panel.add(new Label("Aggiungi nuovo tag:"));
        panel.add(newTagBox);
        panel.add(addTagButton);

        panel.add(feedbackLabel);
        panel.add(saveButton);
        panel.add(backButton);
    }

    private void setupHandlers() {
        contentBox.addKeyUpHandler(event -> {
            int length = contentBox.getText().length();
            if (length > 280) {
                contentBox.setText(contentBox.getText().substring(0, 280));
            }
            charCountLabel.setText(length + " / 280");
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

        saveButton.addClickHandler(event -> {
            String title = titleBox.getText().trim();
            String content = contentBox.getText().trim();

            JSONArray tagsArray = new JSONArray();
            int tagIndex = 0; // usato per tenere traccia dell'indice compatto dell'array

            for (int i = 0; i < tagListBox.getItemCount(); i++) {
                if (tagListBox.isItemSelected(i)) {
                    String tagValue = tagListBox.getValue(i);
                    if (tagValue != null && !tagValue.trim().isEmpty()) {
                        tagsArray.set(tagIndex++, new JSONString(tagValue.trim()));
                    }
                }
            }

            if (title.isEmpty() || content.isEmpty()) {
                Window.alert("Titolo e contenuto sono obbligatori.");
                return;
            }

            JSONObject payload = new JSONObject();
            payload.put("title", new JSONString(title));
            payload.put("content", new JSONString(content));
            payload.put("tags", tagsArray);
            payload.put("ownerEmail", new JSONString("test@gmail.com"));

            RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, GWT.getHostPageBaseURL() + "api/notes");
            builder.setHeader("Content-Type", "application/json");
            try {
                builder.sendRequest(payload.toString(), new RequestCallback() {
                    @Override
                    public void onResponseReceived(Request request, Response response) {
                        if (response.getStatusCode() == Response.SC_OK) {
                            feedbackLabel.setText(noteLogName + " Created!");
                        } else if (response.getStatusCode() == Response.SC_CONFLICT) {
                            feedbackLabel.setText(noteLogName + " already exists.");
                        } else {
                            feedbackLabel.setText(noteLogName + " Creation failed: " + response.getText());
                        }

                        clearForm();
                    }

                    @Override
                    public void onError(Request request, Throwable exception) {
                        feedbackLabel.setText("Error: " + exception.getMessage());
                    }
                });
            } catch (RequestException e) {
                feedbackLabel.setText("Request error: " + e.getMessage());
            }
        });

        backButton.addClickHandler(event -> {
            panel.clear();
            panel.add(new ViewNotesPanel());
        });
    }

    private void clearForm() {
        titleBox.setText("");
        contentBox.setText("");
        for (int i = 0; i < tagListBox.getItemCount(); i++) {
            tagListBox.setItemSelected(i, false);
        }
        charCountLabel.setText("0 / 280");
    }

    private void updateTagList(Boolean exists, String newTag) {
        if (!exists) {
            // Usiamo l'indice come valore, ma si può usare anche un UUID o il testo stesso
            tagListBox.addItem(newTag, String.valueOf(tagListBox.getItemCount() + 1));
            newTagBox.setText("");
        } else {
            feedbackLabel.setText("Tag già presente.");
        }
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
}
