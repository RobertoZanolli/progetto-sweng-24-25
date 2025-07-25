package com.google.gwt.sample.notes.client;

import java.util.ArrayList;
import java.util.List;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.sample.notes.shared.Note;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.user.datepicker.client.DateBox.DefaultFormat;
import com.google.gwt.i18n.client.DateTimeFormat;

public class ViewNotesPanel extends Composite {
    private final VerticalPanel bodyPanle = new VerticalPanel();
    private List<Note> notes = new ArrayList<>();
    private List<Note> filteredNotes = new ArrayList<>();
    private String currentKeyword = "";
    private List<String> currentSelectedTags = new ArrayList<>();
    private final Label feedbackLabel = new Label();
    private final TextBox searchBox = new TextBox();
    private final Button createNoteButton = new Button("Nuova Nota");
    private final Button exitButton = new Button("Esci");
    private final Button refreshButton = new Button("Aggiorna Lista");
    private final Button removeAllFiltersButton = new Button("Rimuovi tutti i filtri");
    private final DateBox startDate = new DateBox();
    private final DateBox endDate = new DateBox();
    private final VerticalPanel viewNotesPanel = new VerticalPanel();
    private final Button btnDeleteDateFilter = new Button("Rimuovi filtro data modifica");
    private final DateBox startCreatedDate = new DateBox();
    private final DateBox endCreatedDate = new DateBox();
    private final Button btnDeleteCreatedDateFilter = new Button("Rimuovi filtro data creazione");

    @SuppressWarnings("deprecation")
    private final ListBox tagListBox = new ListBox(true);

    public ViewNotesPanel() {
        initWidget(bodyPanle);
        buildUI();
        setupHandlers();
    }

    private void buildUI() {
        // Sezione di ricerca per parole chiave
        HorizontalPanel searchPanel = new HorizontalPanel();

        bodyPanle.add(new Label("Utente loggato: " + Session.getInstance().getUserEmail()));

        searchPanel.setSpacing(10);
        searchBox.getElement().setPropertyString("placeholder", "Cerca...");
        searchPanel.add(new Label("Cerca per parole chiave: "));
        searchPanel.add(searchBox);
        bodyPanle.add(searchPanel);

        // Sezione di ricerca per tag
        HorizontalPanel tagPanel = new HorizontalPanel();
        tagPanel.setSpacing(10);
        tagPanel.add(new Label("Cerca per tag (usa 'ctrl/cmd+ Click' per selezione multipla o per rimuovere selezione): "));
        getTags();
        tagPanel.add(tagListBox);
        bodyPanle.add(tagPanel);

        // Sezione di ricerca per data di modifica
        DefaultFormat dateFormat = new DateBox.DefaultFormat(DateTimeFormat.getFormat("dd/MM/yyyy"));
        startDate.setFormat(dateFormat);
        endDate.setFormat(dateFormat);
        startDate.getTextBox().getElement().setPropertyBoolean("readOnly", true);
        endDate.getTextBox().getElement().setPropertyBoolean("readOnly", true);
        startCreatedDate.setFormat(dateFormat);
        endCreatedDate.setFormat(dateFormat);
        startCreatedDate.getTextBox().getElement().setPropertyBoolean("readOnly", true);
        endCreatedDate.getTextBox().getElement().setPropertyBoolean("readOnly", true);

        HorizontalPanel datePanel = new HorizontalPanel();
        datePanel.setSpacing(10);
        datePanel.add(new Label("Cerca per data di modifica: "));
        datePanel.add(new Label("Da: "));
        datePanel.add(startDate);
        datePanel.add(new Label("A: "));
        datePanel.add(endDate);
        datePanel.add(btnDeleteDateFilter);
        bodyPanle.add(datePanel);

        HorizontalPanel createdDatePanel = new HorizontalPanel();
        createdDatePanel.setSpacing(10);
        createdDatePanel.add(new Label("Cerca per data di creazione: "));
        createdDatePanel.add(new Label("Da: "));
        createdDatePanel.add(startCreatedDate);
        createdDatePanel.add(new Label("A: "));
        createdDatePanel.add(endCreatedDate);
        createdDatePanel.add(btnDeleteCreatedDateFilter);
        bodyPanle.add(createdDatePanel);

        getNotes();

        // Bottone per la navigazione
        HorizontalPanel buttonPanel = new HorizontalPanel();
        buttonPanel.setSpacing(10);
        buttonPanel.add(createNoteButton);
        buttonPanel.add(removeAllFiltersButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(exitButton);
        bodyPanle.add(feedbackLabel);
        bodyPanle.add(buttonPanel);

        // Pannello per visualizzare le note
        bodyPanle.setSpacing(10);
        bodyPanle.add(viewNotesPanel);
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

        startDate.addValueChangeHandler(event -> {
            if (endDate.getValue() != null && startDate.getValue() != null) {
                // Controllo se la data di inizio è precedente alla data di fine
                boolean isValidDate = startDate.getValue().before(endDate.getValue());
                if (!isValidDate) {
                    startDate.setValue(null);
                    Window.alert("La data di inizio deve essere precedente alla data di fine.");
                    return;
                }
            }
            // Filtra note per data di inizio
            applyFilters();
        });

        endDate.addValueChangeHandler(event -> {
            if (startDate.getValue() != null && endDate.getValue() != null) {
                // Controllo se la data di fine è successiva alla data di inizio
                boolean isValidDate = endDate.getValue().after(startDate.getValue());
                if (!isValidDate) {
                    endDate.setValue(null);
                    Window.alert("La data di fine deve essere successiva alla data di inizio.");
                    return;
                }
            }
            // Filtra note per data di inizio
            applyFilters();
        });

        btnDeleteDateFilter.addClickHandler(event -> {
            startDate.setValue(null);
            endDate.setValue(null);
            // Quando rimuovo filtro su data per il refresh devo rimettere gli altri filtri se c'erano
            currentKeyword = searchBox.getText().toLowerCase();
            currentSelectedTags.clear();
            for (int i = 0; i < tagListBox.getItemCount(); i++) {
                if (tagListBox.isItemSelected(i)) {
                    currentSelectedTags.add(tagListBox.getItemText(i).toLowerCase());
                }
            }
            applyFilters();
        });

        // Gestione filtro data di creazione
        startCreatedDate.addValueChangeHandler(event -> {
            if (endCreatedDate.getValue() != null && startCreatedDate.getValue() != null) {
                if (!startCreatedDate.getValue().before(endCreatedDate.getValue())) {
                    startCreatedDate.setValue(null);
                    Window.alert("La data di inizio creazione deve essere precedente alla data di fine.");
                    return;
                }
            }
            applyFilters();
        });
        endCreatedDate.addValueChangeHandler(event -> {
            if (startCreatedDate.getValue() != null && endCreatedDate.getValue() != null) {
                if (!endCreatedDate.getValue().after(startCreatedDate.getValue())) {
                    endCreatedDate.setValue(null);
                    Window.alert("La data di fine creazione deve essere successiva alla data di inizio.");
                    return;
                }
            }
            applyFilters();
        });
        btnDeleteCreatedDateFilter.addClickHandler(event -> {
            startCreatedDate.setValue(null);
            endCreatedDate.setValue(null);
            // Mantieni gli altri filtri
            currentKeyword = searchBox.getText().toLowerCase();
            currentSelectedTags.clear();
            for (int i = 0; i < tagListBox.getItemCount(); i++) {
                if (tagListBox.isItemSelected(i)) {
                    currentSelectedTags.add(tagListBox.getItemText(i).toLowerCase());
                }
            }
            applyFilters();
        });

        removeAllFiltersButton.addClickHandler(event -> {
            
            searchBox.setText("");
            currentKeyword = "";
            
            
            for (int i = 0; i < tagListBox.getItemCount(); i++) {
                tagListBox.setItemSelected(i, false);
            }
            currentSelectedTags.clear();
            
            
            startDate.setValue(null);
            endDate.setValue(null);
            startCreatedDate.setValue(null);
            endCreatedDate.setValue(null);
            
            applyFilters();
        });

        createNoteButton.addClickHandler(event -> {
            RootPanel.get("mainPanel").clear();
            RootPanel.get("mainPanel").add(new CreateNotePanel());
        });

        exitButton.addClickHandler(event -> {
            RootPanel.get("mainPanel").clear();
            RootPanel.get("mainPanel").add(new HomePanel());

            Session.getInstance().destroy();
        });

        refreshButton.addClickHandler(event -> {
            getNotes();
        });
    }

    private void applyFilters() {
        filteredNotes = ViewNotesFilter.filter(
            notes,
            currentKeyword,
            currentSelectedTags,
            startDate.getValue(),
            endDate.getValue(),
            startCreatedDate.getValue(),
            endCreatedDate.getValue()
        );
        renderNotes();
    }

    // Mostra le note filtrate
    private void renderNotes() {
        viewNotesPanel.clear();
        for (Note note : filteredNotes) {
            viewNotesPanel.add(createNoteWidget(note));
            viewNotesPanel.add(new HTMLPanel("<hr>"));
        }
    }

    /**
     * Crea un pannello per visualizzare una singola nota.
     * @param note la nota da visualizzare
     * @return pannello contenente i dettagli della nota e il bottone per il dettaglio
     */
    private VerticalPanel createNoteWidget(Note note) {
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

        Label updatedAtLabel = new Label("Ultima modifica: "
            + (note.getCurrentVersion().getUpdatedAt() != null
                ? note.getCurrentVersion().getUpdatedAt().toString()
                : "Data non disponibile"));
        notePanel.add(updatedAtLabel);

        Label ownerLabel = new Label("Proprietario: " + note.getOwnerEmail());
        notePanel.add(ownerLabel);

        Button noteDetailButton = new Button("Vedi nota");
        noteDetailButton.addClickHandler(event -> {
            RootPanel.get("mainPanel").clear();
            RootPanel.get("mainPanel").add(new NoteDetailPanel(note));
        });
        notePanel.add(noteDetailButton);

        return notePanel;
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
                        notes = JsonParserUtil.parseNotesJson(json);
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
                        List<String> tags = JsonParserUtil.parseTagsJson(json);

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

}