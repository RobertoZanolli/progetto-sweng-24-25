package com.google.gwt.sample.notes.server;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;

import java.io.File;

public abstract class AbstractDB<K, V> {
    protected DB db;
    protected HTreeMap<K, V> map;
    protected String tableName;

    public AbstractDB(File dbFile, String tableName, Serializer<K> keySerializer, Serializer<V> valueSerializer) {
        this.tableName = tableName;
        try {
            db = DBMaker.fileDB(dbFile).fileMmapPreclearDisable().transactionEnable().make();
            map = db.hashMap(tableName, keySerializer, valueSerializer).createOrOpen();
        } catch (Exception e) {
            map = null;
            db = null;
            e.printStackTrace();
        }
    }

    public String getTableName() {
        return tableName;
    }
    
    public HTreeMap<K, V> getMap() {
        return map;
    }

    public void commit() {
        if (db != null) db.commit();
    }

    public void close() {
        if (db != null) {
            db.close();
            db = null;
        }
        map = null;
    }
}