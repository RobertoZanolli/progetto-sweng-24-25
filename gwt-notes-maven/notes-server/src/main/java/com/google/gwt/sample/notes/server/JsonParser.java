package com.google.gwt.sample.notes.server;

public interface JsonParser {
    <T> T fromJson(String json, Class<T> clazz);
    String toJson(Object obj);
}
