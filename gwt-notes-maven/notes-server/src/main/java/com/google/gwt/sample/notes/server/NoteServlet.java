package com.google.gwt.sample.notes.server;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gwt.sample.notes.shared.Note;
import com.google.gwt.sample.notes.shared.Session;
import com.google.gwt.sample.notes.shared.Tag;
import com.google.gwt.sample.notes.shared.Version;
import com.google.gwt.sample.notes.shared.Permission;

import org.mapdb.HTreeMap;

import javax.servlet.http.*;
import java.io.*;
import java.util.List;
import java.util.ArrayList;

public class NoteServlet extends HttpServlet {
    private File dbFileNote = null;
    private File dbFileTag = null;
    private final String noteTableName = "notes";
    private final String noteLogName = "Note";
    private final String tagTableName = "tags";
    private final String tagLogName = "Tag";
    private TagDB tagDB;
    private NoteDB noteDB;
    private Session session;

    public NoteServlet() {
        this.session = Session.getInstance();
    }

    public NoteServlet(File dbFileNote, File dbFileTag) {
        this.dbFileNote = dbFileNote;
        this.dbFileTag = dbFileTag;
        this.session = Session.getInstance();
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
        String userEmail = this.session.getUserEmail();

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

        // Filtra le note in base ai permessi e allo stato nascosto per l'utente
        List<Note> visibleNotes = new ArrayList<>();
        for (Note note : noteMap.values()) {
            if (note.getPermission().canView(userEmail, note)) {
                visibleNotes.add(note);
            }
        }

        // Usa Gson per convertire la lista filtrata in JSON
        Gson gson = new Gson();
        String json = gson.toJson(visibleNotes);

        // Scrivi nella risposta
        PrintWriter out = resp.getWriter();
        out.print(json);
        out.flush();

        // Imposta stato OK
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String userEmail = this.session.getUserEmail();

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

        Note noteToDelete = noteMap.get(noteId);
        // Controllo permessi
        if (!noteToDelete.getPermission().canEdit(userEmail, noteToDelete)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getWriter().write("User " + userEmail + " does not have permission to delete note " + noteId);
            return;
        }

        noteMap.remove(noteId);
        this.noteDB.commit();

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write(noteLogName + " with ID " + noteId + " deleted");
    }


    @SuppressWarnings("deprecation")
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String userEmail = this.session.getUserEmail();

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

        Note noteToUpdate = noteMap.get(noteId);

        // Controllo permessi
        if (!noteToUpdate.getPermission().canEdit(userEmail, noteToUpdate)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getWriter().write("User " + userEmail + " does not have permission to update note " + noteId);
            return;
        }

        Version newVersion = null;
        String[] newTags = null;
        Permission newPermission = null;
        try {
            String strVersion = req.getReader().lines().reduce("", (accumulator, actual) -> accumulator + actual);
            JsonObject jsonObj = new JsonParser().parse(strVersion).getAsJsonObject();

            // Aggiorna tag se presenti nel JSON
            if (jsonObj.has("tags") && jsonObj.get("tags").isJsonArray()) {
                JsonArray tagsArray = jsonObj.getAsJsonArray("tags");
                newTags = new String[tagsArray.size()];
                for (int i = 0; i < tagsArray.size(); i++) {
                    newTags[i] = tagsArray.get(i).getAsString();
                }
            }

            // Aggiorna permesso se presente nel JSON
            if (jsonObj.has("permission") && !jsonObj.get("permission").isJsonNull()) {
                String permString = jsonObj.get("permission").getAsString();
                newPermission = Permission.valueOf(permString);
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
        if (newPermission != null) {
            existingNote.setPermission(newPermission);
        }

        existingNote.newVersion(newVersion);

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