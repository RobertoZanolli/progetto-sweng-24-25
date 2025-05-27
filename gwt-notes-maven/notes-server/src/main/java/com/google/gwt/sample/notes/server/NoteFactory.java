package com.google.gwt.sample.notes.server;

import com.google.gson.Gson;
import com.google.gwt.sample.notes.shared.Note;
import com.google.gwt.sample.notes.shared.User;

import java.util.Date;

public class NoteFactory {
    private static final Gson gson = new Gson();

    // Factory method standard
    public static Note create(String title, String content, String[] tags, User owner) {
        Note note = new Note();
        note.setTitle(title);
        note.setContent(content);
        note.setTags(tags);
        note.setOwner(owner);
        Date now = new Date();
        note.setCreatedDate(now);
        note.setLastModifiedDate(now);
        return note;
    }

    // Factory method da JSON
    public static Note fromJson(String json) {
        Note note = gson.fromJson(json, Note.class);

        // Gestione date null
        Date now = new Date();
        if (note.getCreatedDate() == null) note.setCreatedDate(now);
        if (note.getLastModifiedDate() == null) note.setLastModifiedDate(now);

        return note;
    }

    // Per serializzazione
    public static String toJson(Note note) {
        return gson.toJson(note);
    }
}
