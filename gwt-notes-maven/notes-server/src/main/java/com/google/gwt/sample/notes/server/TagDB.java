package com.google.gwt.sample.notes.server;

import java.io.File;

import org.mapdb.Serializer;

import com.google.gwt.sample.notes.shared.Tag;

public class TagDB extends AbstractDB<String, Tag> {
    private static TagDB instance;

    private TagDB(File dbFile) {
        super(dbFile, "tags", Serializer.STRING, Serializer.JAVA);
    }

    public static synchronized TagDB getInstance(File dbFile) {
        if (instance == null) {
            instance = new TagDB(dbFile);
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