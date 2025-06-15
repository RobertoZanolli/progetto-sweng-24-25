package com.google.gwt.sample.notes.server;

import com.google.gwt.sample.notes.shared.ConcreteTag;
import com.google.gwt.sample.notes.shared.Tag;

/**
 * Factory per la creazione e gestione dei tag.
 * Implementa il pattern Singleton per garantire una singola istanza.
 */
public class TagFactory {

    private static final JsonParser parser = new GsonJsonParser();
    private static TagFactory instance;

    private TagFactory() {}

    /**
     * Restituisce l'istanza singleton della factory.
     */
    public static synchronized TagFactory getInstance() {
        if (instance == null) {
            instance = new TagFactory();
        }
        return instance;
    }

    /**
     * Crea un nuovo tag con il nome specificato.
     */
    public static synchronized Tag create(String name) {
        return new ConcreteTag(name);
    }

    /**
     * Crea un tag a partire da una stringa JSON.
     */
    public static synchronized Tag fromJson(String json) {
        return parser.fromJson(json, ConcreteTag.class);
    }

    /**
     * Converte un tag in formato JSON.
     */
    public static synchronized String toJson(Tag tag) {
        return parser.toJson(tag);
    }
}
