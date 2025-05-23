package com.google.gwt.sample.notes.server;

import java.io.File;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;

import com.google.gwt.sample.notes.shared.Note;

public class NoteDB {
    private static NoteDB instance;
    private DB db;
    private HTreeMap<String, Note> noteMap;
    private final String noteTableName = "notes";


    private NoteDB(File dbFile) {
        try {
            db = DBMaker.fileDB(dbFile).fileMmapPreclearDisable().transactionEnable().make();
            noteMap = db.hashMap(noteTableName, Serializer.STRING, Serializer.JAVA).createOrOpen();
        } catch (Exception e) {
            noteMap = null;
            db = null;
            e.printStackTrace();
        }
    }

    public static synchronized NoteDB getInstance(File dbFile) {
        if (instance == null) {
            instance = new NoteDB(dbFile);
        }
        return instance;
    }

    public HTreeMap<String, Note> getNoteMap() {
        return noteMap;
    }

    public void commit() {
        if (db != null) db.commit();
    }

    public void close() {
        if (db != null) {
            db.close();
            db = null;
        }
        instance = null;
        noteMap = null;
    }
}