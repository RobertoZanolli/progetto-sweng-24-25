package com.google.gwt.sample.notes.server;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;

import java.io.File;

/**
 * Classe astratta che gestisce la connessione e le operazioni di base con MapDB.
 * Fornisce funzionalità comuni per la gestione del database per tutte le classi DB.
 */
public abstract class AbstractDB<K, V> {
    protected DB db;
    protected HTreeMap<K, V> map;
    protected String tableName;

    /**
     * Inizializza la connessione al database e crea o apre la tabella specificata.
     * In caso di errore, imposta db e map a null e stampa lo stack trace.
     */
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

    /**
     * Salva le modifiche nel database.
     */
    public void commit() {
        if (db != null) db.commit();
    }

    /**
     * Chiude la connessione al database.
     */
    public void close() {
        if (db != null) {
            db.close();
            db = null;
        }
        map = null;
    }
}