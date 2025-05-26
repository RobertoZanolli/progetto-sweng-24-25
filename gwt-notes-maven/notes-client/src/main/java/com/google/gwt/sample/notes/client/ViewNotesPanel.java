package com.google.gwt.sample.notes.client;

import java.util.ArrayList;
import java.util.List;

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
    private Button createTagButton = new Button("Crea Tag");
    private ListBox tagListBox = new ListBox(true); // selezione multipla
    private Button searchButton = new Button("Cerca");

    public ViewNotesPanel() {
        initWidget(panel);
        setupUI();
        renderNotes();
    }

    public ViewNotesPanel(List<Note> notes) {
        this.notes = notes;
        this.filteredNotes = new ArrayList<>(notes);
        initWidget(panel);
        setupUI();
        renderNotes();
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
        tagPanel.add(tagListBox);
        getTag();
        panel.add(tagPanel);

        // Bottone di ricerca
        panel.add(searchButton);

        // Bottone per la navigazione
        HorizontalPanel buttonPanel = new HorizontalPanel();
        buttonPanel.setSpacing(10);
        buttonPanel.add(createNoteButton);
        buttonPanel.add(createTagButton);
        buttonPanel.add(feedbackLabel);
        panel.add(buttonPanel);

        // Bottone per creare nuova nota (qui puoi collegare la logica di navigazione)
        createNoteButton.addClickHandler(event -> {
            // Ad esempio, puoi rimuovere questa view e aggiungere CreateNotePanel
            // oppure usare un callback per comunicare al parent container di cambiare pannello
            panel.clear();
            panel.add(new CreateNotePanel());
        });

        // Bottone per creare tag (aggiungi il tuo CreateTagPanel o altra UI)
        createTagButton.addClickHandler(event -> {
            // Simile al bottone sopra, cambia pannello o mostra dialog
            panel.clear();
            panel.add(new CreateTagPanel());
        });

        searchButton.addClickHandler(event -> {
            // ToDo
        });
    }

    private void renderNotes() {
        // Rimuovo precedenti note, ma mantengo barra e bottoni
        if (panel.getWidgetCount() > 3) {
            // Tieni solo i primi 3 widget (searchPanel, tagPanel e buttonPanel)
            while (panel.getWidgetCount() > 3) {
                panel.remove(3);
            }
        }

        // Aggiungo le note filtrate
        for (Note note : filteredNotes) {
            HorizontalPanel notePanel = new HorizontalPanel();
            notePanel.setSpacing(10);

            Label titleContent = new Label(note.getTitle() + ": " + note.getContent());
            Label lastMod = new Label("Ultima modifica: " + note.getLastModifiedDate());

            notePanel.add(titleContent);
            notePanel.add(lastMod);
            panel.add(notePanel);
        }
    }

    private void getTag() {
        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET,
                GWT.getHostPageBaseURL() + "createTag");
        builder.setHeader("Content-Type", "application/json");
        try {
            builder.setCallback(new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {
                    if (response.getStatusCode() == Response.SC_OK) {
                        String json = response.getText();
                        List<String> tags = parseJsonArray(json);

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

    // Per aggiornare la lista note (ad esempio dopo creazione/modifica)
    public void setNotes(List<Note> notes) {
        this.notes = notes;
        this.filteredNotes = new ArrayList<>(notes);
        renderNotes();
    }

     public List<String> parseJsonArray(String jsonString) {
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
