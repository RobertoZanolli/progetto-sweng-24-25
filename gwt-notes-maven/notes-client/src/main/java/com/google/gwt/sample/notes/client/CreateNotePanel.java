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

public class CreateNotePanel extends Composite {

    private VerticalPanel panel = new VerticalPanel();
    private TextBox titleBox = new TextBox();
    private TextArea contentBox = new TextArea();
    private ListBox tagListBox = new ListBox(true); // selezione multipla
    private Button saveButton = new Button("Salva nota");
    private Label charCountLabel = new Label("0 / 280");
    private final Label feedbackLabel = new Label();
    private TextBox newTagBox = new TextBox();
    private Button addTagButton = new Button("Aggiungi tag");

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

        panel.add(new Label("Tag (Ctrl+Click per selezione multipla):"));
        tagListBox.addItem("Urgente", "1");
        tagListBox.addItem("Idee", "2");
        tagListBox.addItem("Ricerca", "3");
        panel.add(tagListBox);

        panel.add(new Label("Aggiungi nuovo tag:"));
        panel.add(newTagBox);
        panel.add(addTagButton);

        panel.add(feedbackLabel);
        panel.add(saveButton);
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
                boolean exists = false;
                for (int i = 0; i < tagListBox.getItemCount(); i++) {
                    if (tagListBox.getItemText(i).equalsIgnoreCase(newTag)) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    // Usiamo l'indice come valore, ma potresti usare un UUID o il testo stesso
                    tagListBox.addItem(newTag, String.valueOf(tagListBox.getItemCount() + 1));
                    newTagBox.setText("");
                } else {
                    Window.alert("Tag già presente.");
                }
            } else {
                Window.alert("Inserisci un nome per il tag.");
            }
        });


        saveButton.addClickHandler(event -> {
            String title = titleBox.getText().trim();
            String content = contentBox.getText().trim();

            JSONArray tagsArray = new JSONArray();
            for (int i = 0; i < tagListBox.getItemCount(); i++) {
                if (tagListBox.isItemSelected(i)) {
                    tagsArray.set(i, new JSONString(tagListBox.getValue(i)));
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


            // Regaz ricordatevi di sistemare sempre le entry point
            RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, GWT.getHostPageBaseURL() + "createNote");
            builder.setHeader("Content-Type", "application/json");
            try {
                builder.sendRequest(payload.toString(), new RequestCallback() {
                    @Override
                    public void onResponseReceived(Request request, Response response) {
                        if (response.getStatusCode() == Response.SC_OK) {
                            feedbackLabel.setText("Registration successful!");
                        } else if (response.getStatusCode() == Response.SC_CONFLICT) {
                            feedbackLabel.setText("User already exists.");
                        } else {
                            feedbackLabel.setText("Registration failed: " + response.getText());
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
    }

    private void clearForm() {
        titleBox.setText("");
        contentBox.setText("");
        for (int i = 0; i < tagListBox.getItemCount(); i++) {
            tagListBox.setItemSelected(i, false);
        }
        charCountLabel.setText("0 / 280");
    }
}
