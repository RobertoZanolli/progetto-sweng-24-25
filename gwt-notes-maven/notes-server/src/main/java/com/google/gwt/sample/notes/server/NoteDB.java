package com.google.gwt.sample.notes.server;

import java.io.File;

import org.mapdb.Serializer;

import com.google.gwt.sample.notes.shared.Note;

/**
 * Gestisce il database delle note utilizzando il pattern Singleton.
 * Mantiene una singola istanza del database per tutta l'applicazione.
 */
public class NoteDB extends AbstractDB<String, Note> {
    private static NoteDB instance;

    private NoteDB(File dbFile) {
        super(dbFile, "notes", Serializer.STRING, (Serializer<Note>) Serializer.JAVA);
    }

    /**
     * Restituisce l'istanza singleton del database note.
     * Se non esiste, ne crea una nuova.
     */
    public static synchronized NoteDB getInstance(File dbFile) {
        if (instance == null) {
            instance = new NoteDB(dbFile);
        }
        return instance;
    }

    /**
     * Resetta l'istanza singleton, chiudendo la connessione al database.
     */
    public static synchronized void resetInstance() {
        if (instance != null && instance.db != null) {
            instance.db.close();
        }
        instance = null;
    }
}