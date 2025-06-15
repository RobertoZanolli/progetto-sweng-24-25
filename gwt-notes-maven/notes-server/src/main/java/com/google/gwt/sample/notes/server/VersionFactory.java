package com.google.gwt.sample.notes.server;

import com.google.gwt.sample.notes.shared.ConcreteVersion;
import com.google.gwt.sample.notes.shared.Version;

import java.util.Date;

/**
 * Factory per la creazione e gestione delle versioni delle note.
 * Implementa il pattern Singleton per garantire una singola istanza.
 */
public class VersionFactory {
    private static final JsonParser parser = new GsonJsonParser();
    private static VersionFactory instance;

    private VersionFactory(){}

    /**
     * Restituisce l'istanza singleton della factory.
     */
    public static synchronized VersionFactory getInstance(){
        
        if (instance == null){
            instance = new VersionFactory();
        }
        return instance;

    }

    /**
     * Crea una nuova versione con titolo e contenuto specificati.
     * Imposta automaticamente la data di aggiornamento.
     */
    public static synchronized Version create(String title, String content) {
        Version version = new ConcreteVersion();

        version.setTitle(title);
        version.setContent(content);
        Date now = new Date();
        version.setUpdatedAt(now);

        return version;
    }

    /**
     * Crea una versione a partire da una stringa JSON.
     * Assicura che la data di aggiornamento sia impostata.
     */
    public static synchronized Version fromJson(String json) {
        Version version = parser.fromJson(json, ConcreteVersion.class);

        Date now = new Date();
        if (version.getUpdatedAt() == null) {
            version.setUpdatedAt(now);
        }

        return version;
    }

    /**
     * Converte una versione in formato JSON.
     */
    public static synchronized String toJson(Version version) {
        return parser.toJson(version);
    }
}
