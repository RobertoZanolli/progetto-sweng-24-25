package com.google.gwt.sample.notes.server;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;

public class UserDB {
    private static UserDB instance;
    private DB db;
    private HTreeMap<String, String> users;

    private UserDB(String dbPath) {
        try {
            String pathToUse = dbPath != null ? dbPath : new java.io.File("users.db").getAbsolutePath();
            db = DBMaker.fileDB(pathToUse).make();
            users = (HTreeMap<String, String>) db.hashMap("users").createOrOpen();
        } catch (Exception e) {
            users = null;
            db = null;
            e.printStackTrace();
        }
    }

    public static synchronized UserDB getInstance(String dbPath) {
        if (instance == null) {
            instance = new UserDB(dbPath);
        }
        return instance;
    }

    public HTreeMap<String, String> getUsers() {
        return users;
    }

    public void commit() {
        if (db != null) db.commit();
    }
}