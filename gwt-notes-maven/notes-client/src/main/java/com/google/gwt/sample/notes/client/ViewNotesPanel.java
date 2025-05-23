package com.google.gwt.sample.notes.client;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.sample.notes.shared.Note;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class ViewNotesPanel extends Composite {

    private VerticalPanel panel = new VerticalPanel();
    private List<Note> notes = new ArrayList<>();
    private List<Note> filteredNotes = new ArrayList<>();

    private TextBox searchBox = new TextBox();
    private Button createNoteButton = new Button("Nuova Nota");
    private Button createTagButton = new Button("Crea Tag");

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
        // Barra di ricerca
        HorizontalPanel searchPanel = new HorizontalPanel();
        searchBox.getElement().setPropertyString("placeholder", "Cerca note...");
        searchPanel.add(new Label("Cerca: "));
        searchPanel.add(searchBox);

        // Pulsanti navigazione
        HorizontalPanel buttonPanel = new HorizontalPanel();
        buttonPanel.setSpacing(10);
        buttonPanel.add(createNoteButton);
        buttonPanel.add(createTagButton);

        panel.add(searchPanel);
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
            panel.add(new CreateTagPanel()); // supponendo tu abbia questa classe
        });
    }

    private void renderNotes() {
        // Rimuovo precedenti note, ma mantengo barra e bottoni
        if (panel.getWidgetCount() > 2) {
            // Tieni solo i primi 2 widget (searchPanel e buttonPanel)
            while (panel.getWidgetCount() > 2) {
                panel.remove(2);
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

    // Per aggiornare la lista note (ad esempio dopo creazione/modifica)
    public void setNotes(List<Note> notes) {
        this.notes = notes;
        this.filteredNotes = new ArrayList<>(notes);
        renderNotes();
    }
}
