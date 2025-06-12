
package com.google.gwt.sample.notes.server;

import com.google.gson.Gson;
import com.google.gwt.sample.notes.shared.Tag;
import com.google.gwt.sample.notes.shared.ConcreteTag;

public class TagFactory {

    private static final Gson gson = new Gson();
    private static TagFactory instance;

    private TagFactory() {}

    public static synchronized TagFactory getInstance() {
        if (instance == null) {
            instance = new TagFactory();
        }
        return instance;
    }

    public static synchronized Tag create(String name) {
        return new ConcreteTag(name);
    }

    public static synchronized Tag fromJson(String json) {
        return gson.fromJson(json, ConcreteTag.class);
    }

    public static synchronized String toJson(Tag tag) {
        return gson.toJson(tag);
    }
}
