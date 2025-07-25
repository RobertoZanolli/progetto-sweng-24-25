package com.google.gwt.sample.notes.server;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gwt.sample.notes.shared.ConcreteNote;
import com.google.gwt.sample.notes.shared.Note;
import com.google.gwt.sample.notes.shared.NoteIdGenerator;
import com.google.gwt.sample.notes.shared.Permission;
import com.google.gwt.sample.notes.shared.Version;

import java.util.Date;

/**
 * Factory per la creazione e gestione delle note.
 * Implementa il pattern Singleton e gestisce la serializzazione JSON.
 */
public class NoteFactory {

    private static final GsonJsonParser parser = new GsonJsonParser();
    private static NoteFactory instance;

    private NoteFactory(){}

    /**
     * Restituisce l'istanza singleton della factory.
     */
    public static synchronized NoteFactory getInstance(){
        if (instance == null){
            instance = new NoteFactory();
        }
        return instance;

    }

    /**
     * Crea una nuova nota con parametri specificati.
     * Genera automaticamente un ID univoco e imposta la data di creazione.
     */
    public static synchronized Note create(String title, String content, String[] tags, String ownerEmail, Permission permission) {
        Note note = new ConcreteNote();

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

    /**
     * Crea una nota con ID specificato.
     * Richiede una versione iniziale per evitare NullPointerException.
     */
    public static synchronized Note create(String id, String title, String content, String[] tags, String ownerEmail, Permission permission) {
        Note note = new ConcreteNote();
        note.setId(id);
        note.setTags(tags);
        note.setOwnerEmail(ownerEmail);
        Date now = new Date();
        note.setCreatedAt(now);
        note.setPermission(permission);


        Version initialVersion = VersionFactory.create(title, content);
        note.newVersion(initialVersion);
        return note;
    }

    /**
     * Crea una nota a partire da una stringa JSON.
     * Gestisce la validazione e l'impostazione dei valori di default.
     */
    public static synchronized Note fromJson(String json) {
        Note note = parser.fromJson(json, ConcreteNote.class);


        Date now = new Date();
        if (note.getCreatedAt() == null) {
            note.setCreatedAt(now);
        }

        if (note.getId() == null || note.getId().isEmpty()) {
            NoteIdGenerator generator = new NoteIdGenerator(1);
            long id = generator.nextId();
            note.setId(Long.toString(id));
        }
        
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

    /**
     * Converte una nota in formato JSON.
     */
    public static synchronized String toJson(Note note) {
        return parser.toJson(note);
    }
}
