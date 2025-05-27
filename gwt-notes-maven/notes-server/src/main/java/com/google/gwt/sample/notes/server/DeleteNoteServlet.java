package com.google.gwt.sample.notes.server;

import com.google.gwt.sample.notes.shared.Note;
import org.mapdb.HTreeMap;

import javax.servlet.http.*;
import java.io.*;

public class DeleteNoteServlet extends HttpServlet {
    private File dbFileNote = null;
    private final String noteTableName = "notes";
    private final String noteLogName = "Note";
    private NoteDB noteDB;

    public DeleteNoteServlet() {
        // Default constructor for servlet container
    }

    public DeleteNoteServlet(File dbFileNote) {
        this.dbFileNote = dbFileNote;
    }

    public void setDbFileNote(File dbFile) {
        this.dbFileNote = dbFile;
    }

    @Override
    public void init() {
        this.dbFileNote = dbFileNote != null ? dbFileNote : new File(noteTableName + ".db");
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
        this.noteDB.close();

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write(noteLogName + " with ID " + noteId + " deleted");
    }

    @Override
    public void destroy() {
        if (noteDB != null) {
            noteDB.close();
        }
    }
}