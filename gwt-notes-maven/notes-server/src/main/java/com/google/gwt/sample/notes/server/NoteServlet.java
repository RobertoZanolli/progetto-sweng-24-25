package com.google.gwt.sample.notes.server;

import com.google.gson.Gson;
import com.google.gwt.sample.notes.shared.Note;
import com.google.gwt.sample.notes.shared.Tag;

import org.mapdb.HTreeMap;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;
import java.util.Date;

@WebServlet("/notes")
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
        // Default constructor for servlet container
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
            System.err.println(strNote);
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Invalid " + noteLogName + " data: " + e.getMessage());
            return;
        }
        if (note == null || note.getTitle() == null || note.getTitle().isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Title required");
            return;
        }
        if (noteMap.containsKey(note.getId())) {
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            resp.getWriter().write(noteLogName + " already exists");
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
        System.err.println("ID ricevuto per PUT: " + noteId);

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

        Note updatedNote = null;
        try {
            String strNote = req.getReader().lines().reduce("", (accumulator, actual) -> accumulator + actual);
            updatedNote = NoteFactory.fromJson(strNote);
            System.err.println("Nota aggiornata ricevuta: " + strNote);
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Invalid Note: " + e.getMessage());
            return;
        }

        if (updatedNote == null || updatedNote.getTitle() == null || updatedNote.getTitle().isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Title required");
            return;
        }

        Note existingNote = noteMap.get(noteId);
        if (updatedNote.getOwnerEmail() == null || updatedNote.getOwnerEmail() == null || updatedNote.getOwnerEmail().isEmpty()) {
            updatedNote.setOwnerEmail(existingNote.getOwnerEmail());
        }
        if (updatedNote.getCreatedDate() == null) {
            updatedNote.setCreatedDate(existingNote.getCreatedDate());
        }
        updatedNote.setLastModifiedDate(new Date());

        // Verifica dei tag
        if (updatedNote.getTags() != null) {
            for (String tag : updatedNote.getTags()) {
                if (tag == null || tag.isEmpty()) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write("Tag name required");
                    return;
                }
                if (!tagMap.containsKey(tag)) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write("Tag " + tag + " does not exist");
                    return;
                }
            }
        }

        // Salvo la nota aggiornata
        noteMap.put(noteId, updatedNote);
        this.noteDB.commit();

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write("Note updated");
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