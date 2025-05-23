package com.google.gwt.sample.notes.server;

import java.io.File;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;

import com.google.gwt.sample.notes.shared.Tag;

public class TagDB {
    private static TagDB instance;
    private DB db;
    private HTreeMap<String, Tag> tagMap;
    private final String tagTableName = "tags";

    private TagDB(File dbFile) {
        try {
            db = DBMaker.fileDB(dbFile).fileMmapPreclearDisable().transactionEnable().make();
            tagMap = db.hashMap(tagTableName, Serializer.STRING, Serializer.JAVA).createOrOpen();
        } catch (Exception e) {
            tagMap = null;
            db = null;
            e.printStackTrace();
        }
    }

    public static synchronized TagDB getInstance(File dbFile) {
        if (instance == null) {
            instance = new TagDB(dbFile);
        }
        return instance;
    }

    public HTreeMap<String, Tag> getTagMap() {
        return tagMap;
    }

    public void commit() {
        if (db != null)
            db.commit();
    }

    public void close() {
        if (db != null) {
            db.close();
            db = null;
        }
        instance = null;
        tagMap = null;
    }
}