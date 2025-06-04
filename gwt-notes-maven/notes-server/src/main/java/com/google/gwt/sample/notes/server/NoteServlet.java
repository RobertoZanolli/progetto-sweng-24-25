package com.google.gwt.sample.notes.server;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gwt.sample.notes.shared.Note;
import com.google.gwt.sample.notes.shared.Tag;
import com.google.gwt.sample.notes.shared.Version;

import org.mapdb.HTreeMap;

import javax.servlet.http.*;
import java.io.*;

public class NoteServlet extends HttpServlet {
    private File dbFileNote = null;
    private File dbFileTag = null;
    private final String noteTableName = "notes";
    private final String noteLogName = "Note";
    private final String tagTableName = "tags";
    private final String tagLogName = "Tag";
    private TagDB tagDB;
    private NoteDB noteDB;

    public NoteServlet() {
    }

    public NoteServlet(File dbFileNote, File dbFileTag) {
        this.dbFileNote = dbFileNote;
        this.dbFileTag = dbFileTag;
    }

    public void setDbFileNote(File dbFile) {
        this.dbFileNote = dbFile;
    }

    public void setDbPathTag(File dbFile) {
        this.dbFileTag = dbFile;
    }

    @Override
    public void init() {
        this.dbFileTag = dbFileTag != null ? dbFileTag : new File(tagTableName + ".db");
        this.dbFileNote = dbFileNote != null ? dbFileNote : new File(noteTableName + ".db");

        // use only to remake the database
        /*
         * if (dbFileNote.exists()) {
         * dbFileNote.delete();
         * }
         */
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        this.tagDB = TagDB.getInstance(this.dbFileTag);
        this.noteDB = NoteDB.getInstance(this.dbFileNote);
        HTreeMap<String, Tag> tagMap = tagDB.getMap();
        HTreeMap<String, Note> noteMap = noteDB.getMap();

        if (noteMap == null) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(noteTableName + " database not initialized");
            return;
        }

        if (tagMap == null) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(tagTableName + " database not initialized");
            return;
        }

        Note note = null;
        try {
            String strNote = req.getReader().lines().reduce("", (accumulator, actual) -> accumulator + actual);
            note = NoteFactory.fromJson(strNote);
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Invalid " + noteLogName + " data: " + e.getMessage());
            return;
        }

        if (note == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Invalid note data");
            return;
        }

        if (note.getId() == null || note.getId().isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Note ID required");
            return;
        }

        if (noteMap.containsKey(note.getId())) {
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            resp.getWriter().write(noteLogName + " already exists");
            return;
        }

        if (note.getAllVersions() == null || note.getAllVersions().isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("At least one version required");
            return;
        }

        if (note == null || note.getCurrentVersion() == null || note.getCurrentVersion().getTitle() == null || note.getCurrentVersion().getTitle().isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Title required");
            return;
        }

        // Verifico se i tag esistono
        if (note.getTags() != null) {
            for (String tag : note.getTags()) {
                if (tag == null || tag.isEmpty()) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write(tagLogName + " name required");
                    return;
                }
                if (!tagMap.containsKey(tag)) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write("Tag " + tag + " does not exist");
                    return;
                }
            }
        }

        noteMap.put(note.getId(), note);
        this.noteDB.commit();
        
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write(noteLogName + " created");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        this.noteDB = NoteDB.getInstance(this.dbFileNote);
        HTreeMap<String, Note> noteMap = noteDB.getMap();

        if (noteMap == null) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("Notes database not initialized.");
            return;
        }

        // Prepara risposta JSON
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        // Usa Gson per convertire la collezione di note in JSON
        Gson gson = new Gson();
        String json = gson.toJson(noteMap.values());

        // Scrivi nella risposta
        PrintWriter out = resp.getWriter();
        out.print(json);
        out.flush();

        // Imposta stato OK
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        this.noteDB = NoteDB.getInstance(this.dbFileNote);
        HTreeMap<String, Note> noteMap = noteDB.getMap();

        if (noteMap == null) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(noteTableName + " database not initialized");
            return;
        }

        String noteId = req.getParameter("id");
        if (noteId == null || noteId.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Note ID required");
            return;
        }

        if (!noteMap.containsKey(noteId)) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write(noteLogName + " with ID " + noteId + " not found");
            return;
        }

        noteMap.remove(noteId);
        this.noteDB.commit();

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write(noteLogName + " with ID " + noteId + " deleted");
    }


    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        this.tagDB = TagDB.getInstance(this.dbFileTag);
        this.noteDB = NoteDB.getInstance(this.dbFileNote);
        HTreeMap<String, Tag> tagMap = tagDB.getMap();
        HTreeMap<String, Note> noteMap = noteDB.getMap();

        if (noteMap == null || tagMap == null) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("Database not initialized.");
            return;
        }
        String noteId = req.getParameter("id");


        if (noteId == null || noteId.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Missing or invalid note ID");
            return;
        }

        if (!noteMap.containsKey(noteId)) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("Note not found.");
            return;
        }

        Version newVersion = null;
        String[] newTags = null;
        try {
            String strVersion = req.getReader().lines().reduce("", (accumulator, actual) -> accumulator + actual);

            JsonObject jsonObj = new JsonParser().parse(strVersion).getAsJsonObject();
            if (jsonObj.has("tags") && jsonObj.get("tags").isJsonArray()) {
                JsonArray tagsArray = jsonObj.getAsJsonArray("tags");
                newTags = new String[tagsArray.size()];
                for (int i = 0; i < tagsArray.size(); i++) {
                    newTags[i] = tagsArray.get(i).getAsString();
                }
            }
            newVersion = VersionFactory.fromJson(strVersion);
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Invalid Version: " + e.getMessage());
            return;
        }

        if (newVersion == null || newVersion.getTitle() == null || newVersion.getTitle().isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Title required");
            return;
        }

        Note existingNote = noteMap.get(noteId);


        if (newTags != null) {
            for (String tag : newTags) {
                if (tag == null || tag.isEmpty()) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write(tagLogName + " name required");
                    return;
                }
                if (!tagMap.containsKey(tag)) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write("Tag " + tag + " does not exist");
                    return;
                }
            }
            existingNote.setTags(newTags);
        }

        existingNote.addVersion(newVersion);

        noteMap.put(noteId, existingNote);
        this.noteDB.commit();

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write("Note updated with new version");
    }

    @Override
    public void destroy() {
        if (noteDB != null) {
            noteDB.close();
        }
        if (tagDB != null) {
            tagDB.close();
        }
    }
}