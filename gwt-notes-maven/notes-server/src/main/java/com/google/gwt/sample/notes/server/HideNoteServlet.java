package com.google.gwt.sample.notes.server;

import com.google.gwt.sample.notes.shared.Note;
import com.google.gwt.sample.notes.shared.Session;

import org.mapdb.HTreeMap;

import javax.servlet.http.*;
import java.io.*;

public class HideNoteServlet extends HttpServlet {
    private File dbFileNote = null;
    private final String noteTableName = "notes";
    private NoteDB noteDB;
    private Session session;

    public HideNoteServlet() {
        this.session = Session.getInstance();
    }

    public HideNoteServlet(File dbFileNote) {
        this.dbFileNote = dbFileNote;
        this.session = Session.getInstance();
    }

    public void setDbFileNote(File dbFile) {
        this.dbFileNote = dbFile;
    }

    @Override
    public void init() {
        this.dbFileNote = dbFileNote != null ? dbFileNote : new File(noteTableName + ".db");
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String userEmail = this.session.getUserEmail();
        this.noteDB = NoteDB.getInstance(this.dbFileNote);
        HTreeMap<String, Note> noteMap = noteDB.getMap();

        String noteId = req.getParameter("id");
        if (noteId == null || !noteMap.containsKey(noteId)) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("Note not found");
            return;
        }
        Note note = noteMap.get(noteId);

        // Controllo se l'owner tenta di nascondere la sua nota
        if (userEmail != null && userEmail.equals(note.getOwnerEmail())) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Owner cannot hide note");
            return;
        }

        // Leggi valore 'hide' dal body
        StringBuilder bodyBuilder = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                bodyBuilder.append(line);
            }
        }
        String body = bodyBuilder.toString().trim();
        if (body.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Missing 'hide' value in body");
            return;
        }
        boolean hide;
        try {
            hide = Boolean.parseBoolean(body);
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Invalid 'hide' value");
            return;
        }

        if (hide) {
            // Solo chi può vedere può nascondere
            if (!note.getPermission().canView(userEmail, note)) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                resp.getWriter().write("User does not have permission to hide this note");
                return;
            }
            note.hideForUser(userEmail);
            noteMap.put(noteId, note);
            noteDB.commit();
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("Note hidden for user");
        }
    }

    @Override
    public void destroy() {
        if (noteDB != null) {
            noteDB.close();
        }
    }
}