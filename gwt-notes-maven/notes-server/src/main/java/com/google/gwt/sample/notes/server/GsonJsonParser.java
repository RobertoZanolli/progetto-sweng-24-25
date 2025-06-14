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

public class GsonJsonParser implements JsonParser {
    private final Gson delegateGson;
    private final Gson gson;

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

    @Override
    public <T> T fromJson(String json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }

    @Override
    public String toJson(Object obj) {
        return gson.toJson(obj);
    }
}
