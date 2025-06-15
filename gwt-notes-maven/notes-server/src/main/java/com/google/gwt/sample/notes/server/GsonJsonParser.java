package com.google.gwt.sample.notes.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import com.google.gwt.sample.notes.shared.Version;
import com.google.gwt.sample.notes.shared.ConcreteVersion;

/**
 * Implementazione di JsonParser che utilizza la libreria Gson.
 * Gestisce la conversione JSON con supporto per la classe Version.
 */
public class GsonJsonParser implements JsonParser {
    private final Gson delegateGson;
    private final Gson gson;

    /**
     * Inizializza il parser JSON per la classe Version che utilizza ConcreteVersion come implementazione.
     */
    public GsonJsonParser() {
        this.delegateGson = new Gson();
        this.gson = new GsonBuilder()
            .registerTypeAdapter(Version.class, new JsonDeserializer<Version>() {
                @Override
                public Version deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                    return delegateGson.fromJson(json, ConcreteVersion.class);
                }
            })
            .create();
    }

    /**
     * Converte una stringa JSON in un oggetto del tipo specificato.
     * Utilizza il parser Gson configurato con il deserializzatore personalizzato.
     */
    @Override
    public <T> T fromJson(String json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }

    /**
     * Converte un oggetto in una stringa JSON.
     */
    @Override
    public String toJson(Object obj) {
        return gson.toJson(obj);
    }
}