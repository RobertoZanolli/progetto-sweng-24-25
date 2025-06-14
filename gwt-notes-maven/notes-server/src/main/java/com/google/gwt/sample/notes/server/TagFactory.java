package com.google.gwt.sample.notes.server;

import com.google.gwt.sample.notes.shared.ConcreteTag;
import com.google.gwt.sample.notes.shared.Tag;

public class TagFactory {

    private static final JsonParser parser = new GsonJsonParser();
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
        return parser.fromJson(json, ConcreteTag.class);
    }

    public static synchronized String toJson(Tag tag) {
        return parser.toJson(tag);
    }
}
