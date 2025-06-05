package com.google.gwt.sample.notes.server;

import com.google.gson.Gson;
import com.google.gwt.sample.notes.shared.Version;

import java.util.Date;

public class VersionFactory {
    private static final Gson gson = new Gson();

    public static Version create(String title, String content) {
        Version version = new Version();

        version.setTitle(title);
        version.setContent(content);
        Date now = new Date();
        version.setUpdatedAt(now);

        return version;
    }

    // Factory method da JSON
    public static Version fromJson(String json) {
        Version version = gson.fromJson(json, Version.class);

        // SPOSTARE CONTROLLI QUI (?)

        Date now = new Date();
        if (version.getUpdatedAt() == null) {
            version.setUpdatedAt(now);
        }

        return version;
    }

    // Per serializzazione
    public static String toJson(Version version) {
        return gson.toJson(version);
    }
}
