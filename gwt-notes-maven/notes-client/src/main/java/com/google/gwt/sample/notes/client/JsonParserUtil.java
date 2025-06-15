package com.google.gwt.sample.notes.client;

import com.google.gwt.json.client.*;
import com.google.gwt.sample.notes.shared.ConcreteNote;
import com.google.gwt.sample.notes.shared.ConcreteVersion;
import com.google.gwt.sample.notes.shared.Note;
import com.google.gwt.sample.notes.shared.Permission;
import com.google.gwt.sample.notes.shared.Version;
import com.google.gwt.i18n.client.DateTimeFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class JsonParserUtil {

    // usato in tutti i pannelli per formattare e leggere le date
    private static final DateTimeFormat dateFormat =
            DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * Converte un JSON array di stringhe in una List<String>.
     * @param jsonText testo JSON, ad es. '["tag1","tag2",...]'
     */
    public static List<String> parseTagsJson(String jsonText) {
        JSONValue parsed = JSONParser.parseStrict(jsonText);
        JSONArray array = parsed.isArray();
        List<String> tags = new ArrayList<>();
        if (array != null) {
            for (int i = 0; i < array.size(); i++) {
                JSONString s = array.get(i).isString();
                if (s != null) {
                    tags.add(s.stringValue());
                }
            }
        }
        return tags;
    }

    /**
     * Converte un oggetto Note in JSON (con versions e tags) nel corrispondente oggetto Note.
     * Associa tutte le versioni e i tag come facevi nei panel.
     * @param jsonText testo JSON con chiavi "id","ownerEmail","permission","versions","tags"
     */
    public static Note parseNoteJson(String jsonText) {
        JSONValue parsed = JSONParser.parseStrict(jsonText);
        JSONObject obj = parsed.isObject();

        // campi semplici
        String id = obj.get("id").isString().stringValue();
        String ownerEmail = obj.get("ownerEmail").isString().stringValue();
        String permStr = obj.get("permission").isString().stringValue();
        Permission perm = Permission.valueOf(permStr);

        // versions
        List<Version> versions = new ArrayList<>();
        JSONArray verArray = obj.get("versions").isArray();
        if (verArray != null) {
            for (int i = 0; i < verArray.size(); i++) {
                JSONObject vObj = verArray.get(i).isObject();
                String title   = vObj.get("title").isString().stringValue();
                String content = vObj.get("content").isString().stringValue();

                Date updatedAt = null;
                JSONString upd = vObj.get("updatedAt") != null ? vObj.get("updatedAt").isString() : null;
                if (upd != null) {
                    updatedAt = dateFormat.parse(upd.stringValue());
                }

                Version v = new ConcreteVersion();
                v.setTitle(title);
                v.setContent(content);
                v.setUpdatedAt(updatedAt);
                versions.add(v);
            }
        }

        // tags
        List<String> tags = new ArrayList<>();
        JSONArray tagArray = obj.get("tags").isArray();
        if (tagArray != null) {
            for (int i = 0; i < tagArray.size(); i++) {
                JSONString s = tagArray.get(i).isString();
                if (s != null) {
                    tags.add(s.stringValue());
                }
            }
        }

        // costruzione Note
        Note note = new ConcreteNote();
        note.setId(id);
        note.setOwnerEmail(ownerEmail);
        note.setPermission(perm);
        for (Version v : versions) {
            note.newVersion(v);
        }
        note.setTags(tags.toArray(new String[0]));
        return note;
    }

    // Se in futuro ti serve anche parse di una lista di note:
    public static List<Note> parseNotesJson(String jsonText) {
        JSONValue parsed = JSONParser.parseStrict(jsonText);
        JSONArray array = parsed.isArray();
        List<Note> notes = new ArrayList<>();
        if (array != null) {
            for (int i = 0; i < array.size(); i++) {
                JSONObject o = array.get(i).isObject();
                notes.add(parseNoteJson(o.toString()));
            }
        }
        return notes;
    }
}