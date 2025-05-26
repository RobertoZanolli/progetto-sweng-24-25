package com.google.gwt.sample.notes.server;

import com.google.gwt.sample.notes.shared.Note;
import com.google.gwt.sample.notes.shared.Tag;
import org.mapdb.HTreeMap;

import javax.servlet.http.*;
import java.io.*;

public class CreateNoteServlet extends HttpServlet {
    private File dbFileNote = null;
    private File dbFileTag = null;
    private final String noteTableName = "notes";
    private final String noteLogName = "Note";
    private final String tagTableName = "tags";
    private final String tagLogName = "Tag";
    private TagDB tagDB;
    private NoteDB noteDB;

    public CreateNoteServlet() {
        // Default constructor for servlet container
    }

    public CreateNoteServlet(File dbFileNote, File dbFileTag) {
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

        // verifico se i tag esistono
        if (note.getTags() != null) {
            for (String tag : note.getTags()) {
                if (tag == null || tag.isEmpty()) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write(tagLogName+" name required");
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
        this.noteDB.close();
        this.tagDB.close();
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write(noteLogName + " created");
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