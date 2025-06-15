package com.google.gwt.sample.notes.server;

import org.mapdb.Serializer;
import java.io.File;
 
/**
 * Gestisce il database degli utenti utilizzando il pattern Singleton.
 * Mantiene una singola istanza del database per tutta l'applicazione.
 */
public class UserDB extends AbstractDB<String, String> {
    private static UserDB instance;
 
    private UserDB(File dbFile) {
        super(dbFile, "users", Serializer.STRING, Serializer.STRING);
    }
 
    /**
     * Restituisce l'istanza singleton del database utenti.
     * Se non esiste, ne crea una nuova.
     */
    public static synchronized UserDB getInstance(File dbFile) {
        if (instance == null) {
            instance = new UserDB(dbFile);
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