package com.google.gwt.sample.notes.server;

import java.io.File;

import org.mapdb.Serializer;

import com.google.gwt.sample.notes.shared.Note;

public class NoteDB extends AbstractDB<String, Note> {
    private static NoteDB instance;

    private NoteDB(File dbFile) {
        super(dbFile, "notes", Serializer.STRING, Serializer.JAVA);
    }

    public static synchronized NoteDB getInstance(File dbFile) {
        if (instance == null) {
            instance = new NoteDB(dbFile);
        }
        return instance;
    }

    public static synchronized void resetInstance() {
        if (instance != null && instance.db != null) {
            instance.db.close();
        }
        instance = null;
    }
}