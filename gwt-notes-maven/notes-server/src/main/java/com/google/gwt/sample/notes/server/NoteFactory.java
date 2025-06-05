package com.google.gwt.sample.notes.server;

import com.google.gson.Gson;
import com.google.gwt.sample.notes.shared.Note;
import com.google.gwt.sample.notes.shared.NoteIdGenerator;
import com.google.gwt.sample.notes.shared.Permission;
import com.google.gwt.sample.notes.shared.Version;

import java.util.Date;

public class NoteFactory {
    private static final Gson gson = new Gson();

    // Factory method standard
    public static Note create(String title, String content, String[] tags, String ownerEmail, Permission permissions) {
        Note note = new Note();

        NoteIdGenerator generator = new NoteIdGenerator(1);
        long id = generator.nextId();
        note.setId(Long.toString(id));
        note.setTags(tags);
        note.setOwnerEmail(ownerEmail);
        Date now = new Date();
        note.setCreatedAt(now);
        note.setPermission(permissions);
        
        Version initialVersion = VersionFactory.create(title, content);
        note.newVersion(initialVersion);
        return note;
    }

    // Factory method con id
    public static Note create(String id, String title, String content, String[] tags, String ownerEmail, Permission permissions) {
        Note note = new Note();
        note.setId(id);
        note.setTags(tags);
        note.setOwnerEmail(ownerEmail);
        Date now = new Date();
        note.setCreatedAt(now);
        note.setPermission(permissions);

        // VERSIONE INIZIALE CI VUOLE SEMPRE O NULL POINTER EXCEPTION 
        // QUANDO INVOCHIAMO NELLA HOME GETCURRENTVERSION()
        Version initialVersion = VersionFactory.create(title, content);
        note.newVersion(initialVersion);
        return note;
    }

    // Factory method da JSON
    public static Note fromJson(String json) {
        Note note = gson.fromJson(json, Note.class);

        Date now = new Date();
        if (note.getCreatedAt() == null) {
            note.setCreatedAt(now);
        }

        if (note.getId() == null || note.getId().isEmpty()) {
            NoteIdGenerator generator = new NoteIdGenerator(1);
            long id = generator.nextId();
            note.setId(Long.toString(id));
        }

        if (note.getCurrentVersion().getUpdatedAt() == null) {
            note.getCurrentVersion().setUpdatedAt(now);
        }

        return note;
    }

    // Per serializzazione
    public static String toJson(Note note) {
        return gson.toJson(note);
    }
}
