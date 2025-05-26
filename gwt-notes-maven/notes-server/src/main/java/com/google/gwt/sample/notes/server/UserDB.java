package com.google.gwt.sample.notes.server;

import org.mapdb.Serializer;
import java.io.File;
 
public class UserDB extends AbstractDB<String, String> {
    private static UserDB instance;
 
    private UserDB(File dbFile) {
        super(dbFile, "users", Serializer.STRING, Serializer.STRING);
    }
 
    public static synchronized UserDB getInstance(File dbFile) {
        if (instance == null) {
            instance = new UserDB(dbFile);
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