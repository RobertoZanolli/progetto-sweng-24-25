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

/**
 * Utility per il parsing dei dati JSON
 */
public class JsonParserUtil {
    /**
     * Converte un array JSON di stringhe in una List<String>
     * @param jsonText 
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
     * Converte un oggetto Note in JSON nel corrispondente oggetto Note
     
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
        
        JSONString crt = obj.get("createdAt").isString();
        Date createdAt = null;
        if (crt != null) {
            String dateStr = crt.stringValue().replace("\u202f", " ");
            createdAt = DateTimeFormat.getFormat("MMM d, yyyy, h:mm:ss a").parse(dateStr);
        }

        // versions
        List<Version> versions = new ArrayList<>();
        JSONArray verArray = obj.get("versions").isArray();
        if (verArray != null) {
            for (int i = 0; i < verArray.size(); i++) {
                JSONObject vObj = verArray.get(i).isObject();
                String title   = vObj.get("title").isString().stringValue();
                String content = vObj.get("content").isString().stringValue();

                JSONString upd = vObj.get("updatedAt").isString();
                Date updatedAt = null;
                if (upd != null) {
                    String dateStr = upd.stringValue().replace("\u202f", " ");
                    updatedAt = DateTimeFormat.getFormat("MMM d, yyyy, h:mm:ss a").parse(dateStr);
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
        note.setCreatedAt(createdAt);
        for (Version v : versions) {
            note.newVersion(v);
        }
        note.setTags(tags.toArray(new String[0]));
        return note;
    }

    /**
     * Converte una lista di note da JSON
     */
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

    /**
     * Converte una singola nota da JSON
     */
    public static Note parseSingleNoteJson(String jsonText) {
        JSONValue value = JSONParser.parseStrict(jsonText);
        JSONObject obj = value.isObject();
        if (obj == null) {
            return null;
        }
        DateTimeFormat singleDateFormat = DateTimeFormat.getFormat("MMM d, yyyy, h:mm:ss a");
        ConcreteNote note = new ConcreteNote();
        // ID
        if (obj.containsKey("id") && obj.get("id").isString() != null) {
            note.setId(obj.get("id").isString().stringValue());
        }
        // OwnerEmail
        if (obj.containsKey("ownerEmail") && obj.get("ownerEmail").isString() != null) {
            note.setOwnerEmail(obj.get("ownerEmail").isString().stringValue());
        }
        // CreatedAt
        if (obj.containsKey("createdAt") && obj.get("createdAt").isString() != null) {
            String dateStr = obj.get("createdAt").isString().stringValue().replace("\u202f", " ");
            note.setCreatedAt(singleDateFormat.parse(dateStr));
        }
        // Tags
        if (obj.containsKey("tags") && obj.get("tags").isArray() != null) {
            JSONArray tagsArray = obj.get("tags").isArray();
            String[] tags = new String[tagsArray.size()];
            for (int i = 0; i < tagsArray.size(); i++) {
                JSONString s = tagsArray.get(i).isString();
                tags[i] = s != null ? s.stringValue() : "";
            }
            note.setTags(tags);
        }
        // Permission
        if (obj.containsKey("permission") && obj.get("permission").isString() != null) {
            note.setPermission(Permission.valueOf(obj.get("permission").isString().stringValue()));
        }
        // Versions
        if (obj.containsKey("versions") && obj.get("versions").isArray() != null) {
            JSONArray verArray = obj.get("versions").isArray();
            for (int i = 0; i < verArray.size(); i++) {
                JSONObject vObj = verArray.get(i).isObject();
                if (vObj != null) {
                    ConcreteVersion v = new ConcreteVersion();
                    if (vObj.containsKey("title") && vObj.get("title").isString() != null) {
                        v.setTitle(vObj.get("title").isString().stringValue());
                    }
                    if (vObj.containsKey("content") && vObj.get("content").isString() != null) {
                        v.setContent(vObj.get("content").isString().stringValue());
                    }
                    if (vObj.containsKey("updatedAt") && vObj.get("updatedAt").isString() != null) {
                        String dateStr = vObj.get("updatedAt").isString().stringValue().replace("\u202f", " ");
                        v.setUpdatedAt(singleDateFormat.parse(dateStr));
                    }
                    note.newVersion(v);
                }
            }
        }
        return note;
    }
}