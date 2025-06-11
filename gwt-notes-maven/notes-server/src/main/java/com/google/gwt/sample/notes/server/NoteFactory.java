package com.google.gwt.sample.notes.server;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.JsonObject;
import com.google.gwt.sample.notes.shared.Note;
import com.google.gwt.sample.notes.shared.NoteIdGenerator;
import com.google.gwt.sample.notes.shared.Permission;
import com.google.gwt.sample.notes.shared.Version;

import java.util.Date;

public class NoteFactory {
    private static final Gson gson = new Gson();
    private static NoteFactory instance;

    private NoteFactory(){}

    public synchronized NoteFactory getInstance(){

        if (instance == null){
            instance = new NoteFactory();
        }
        return instance;

    }

    // Factory method standard
    public static synchronized Note create(String title, String content, String[] tags, String ownerEmail, Permission permission) {
        Note note = new Note();

        NoteIdGenerator generator = new NoteIdGenerator(1);
        long id = generator.nextId();
        note.setId(Long.toString(id));
        note.setTags(tags);
        note.setOwnerEmail(ownerEmail);
        Date now = new Date();
        note.setCreatedAt(now);
        note.setPermission(permission);

        Version initialVersion = VersionFactory.create(title, content);
        note.newVersion(initialVersion);
        return note;
    }

    // Factory method con id
    public static synchronized Note create(String id, String title, String content, String[] tags, String ownerEmail, Permission permission) {
        Note note = new Note();
        note.setId(id);
        note.setTags(tags);
        note.setOwnerEmail(ownerEmail);
        Date now = new Date();
        note.setCreatedAt(now);
        note.setPermission(permission);

        // VERSIONE INIZIALE CI VUOLE SEMPRE O NULL POINTER EXCEPTION
        // QUANDO INVOCHIAMO NELLA HOME GETCURRENTVERSION()
        Version initialVersion = VersionFactory.create(title, content);
        note.newVersion(initialVersion);
        return note;
    }

    // Factory method da JSON
    public static synchronized Note fromJson(String json) {
        Note note = gson.fromJson(json, Note.class);

        // SPOSTARE CONTROLLI QUI (?)

        Date now = new Date();
        if (note.getCreatedAt() == null) {
            note.setCreatedAt(now);
        }

        if (note.getId() == null || note.getId().isEmpty()) {
            NoteIdGenerator generator = new NoteIdGenerator(1);
            long id = generator.nextId();
            note.setId(Long.toString(id));
        }

/*         if (note.getOwnerEmail() == null) {
            note.setOwnerEmail();
        } */
        
        if(note.getCurrentVersion().getUpdatedAt()==null){
            note.getCurrentVersion().setUpdatedAt(now);
        }

        // Imposta il permesso dal JSON o usa default PRIVATE se mancante
        if (note.getPermission() == null) {
            try {
                JsonObject jsonObj = JsonParser.parseString(json).getAsJsonObject();
                if (jsonObj.has("permission") && !jsonObj.get("permission").isJsonNull()) {
                    String permString = jsonObj.get("permission").getAsString();
                    note.setPermission(Permission.valueOf(permString));
                } else {
                    note.setPermission(Permission.PRIVATE);
                }
            } catch (Exception e) {
                note.setPermission(Permission.PRIVATE);
            }
        }

        return note;
    }

    // Per serializzazione
    public static synchronized String toJson(Note note) {
        return gson.toJson(note);
    }
}
