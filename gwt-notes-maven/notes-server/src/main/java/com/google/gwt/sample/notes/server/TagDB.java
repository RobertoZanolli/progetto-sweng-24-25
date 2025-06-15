package com.google.gwt.sample.notes.server;

import java.io.File;

import org.mapdb.Serializer;

import com.google.gwt.sample.notes.shared.Tag;

/**
 * Gestisce il database dei tag utilizzando il pattern Singleton.
 * Mantiene una singola istanza del database per tutta l'applicazione.
 */
public class TagDB extends AbstractDB<String, Tag> {
    private static TagDB instance;

    private TagDB(File dbFile) {
        super(dbFile, "tags", Serializer.STRING, (Serializer<Tag>) Serializer.JAVA);
    }

    /**
     * Restituisce l'istanza singleton del database tag.
     * Se non esiste, ne crea una nuova.
     */
    public static synchronized TagDB getInstance(File dbFile) {
        if (instance == null) {
            instance = new TagDB(dbFile);
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