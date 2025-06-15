package com.google.gwt.sample.notes.server;

import com.google.gson.Gson;
import com.google.gwt.sample.notes.shared.Note;

import javax.servlet.http.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servlet per gestire le richieste relative alle note.
 * Implementa le operazioni CRUD tramite HTTP.
 */
public class NoteServlet extends HttpServlet {
    private File dbFileNote = null;
    private File dbFileTag = null;
    private final String noteTableName = "notes";
    private final String tagTableName = "tags";
    private NoteService noteService; 

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

        // Initialize DBs and then the Service
        NoteDB noteDB = NoteDB.getInstance(this.dbFileNote);
        TagDB tagDB = TagDB.getInstance(this.dbFileTag);
        this.noteService = new NoteService(noteDB, tagDB);
    }

    /**
     * Gestisce la creazione di una nuova nota (POST).
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String userEmail = getUserEmailFromSession(req);
        String noteJson;
        try {
            noteJson = req.getReader().lines().collect(Collectors.joining());
        } catch (IOException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Dati della nota non validi");
            return;
        }
        try {
            noteService.createNote(noteJson, userEmail);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("Nota creata con successo");
        } catch (ServiceException e) {
            resp.setStatus(e.getStatusCode());
            resp.getWriter().write(e.getMessage());
        }
    }

    /**
     * Gestisce il recupero di una nota o di tutte le note visibili (GET).
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String userEmail = getUserEmailFromSession(req);
        String noteId = req.getParameter("id");

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            if (noteId != null && !noteId.isEmpty()) {
                Note note = noteService.getNoteById(noteId, userEmail);
                String json = new Gson().toJson(note);
                resp.getWriter().write(json);
            } else {
                List<Note> visibleNotes = noteService.getAllVisibleNotes(userEmail);
                String json = new Gson().toJson(visibleNotes);
                resp.getWriter().write(json);
            }
            resp.setStatus(HttpServletResponse.SC_OK);
        } catch (ServiceException e) {
            resp.setStatus(e.getStatusCode());
            resp.getWriter().write(e.getMessage());
        }
    }

    /**
     * Gestisce l'eliminazione di una nota (DELETE).
     */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String userEmail = getUserEmailFromSession(req);
        String noteId = req.getParameter("id");

        try {
            noteService.deleteNote(noteId, userEmail);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("Nota con ID " + noteId + " eliminata con successo");
        } catch (ServiceException e) {
            resp.setStatus(e.getStatusCode());
            resp.getWriter().write(e.getMessage());
        }
    }

    /**
     * Gestisce l'aggiornamento di una nota (PUT).
     */
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String userEmail = getUserEmailFromSession(req);
        String noteId = req.getParameter("id");
        String updateJson;
        try {
            updateJson = req.getReader().lines().collect(Collectors.joining());
        } catch (IOException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Dati di aggiornamento non validi");
            return;
        }
        try {
            noteService.updateNote(noteId, updateJson, userEmail);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("Nota aggiornata con nuova versione");
        } catch (ServiceException e) {
            resp.setStatus(e.getStatusCode());
            resp.getWriter().write(e.getMessage());
        }
    }

    private String getUserEmailFromSession(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return (session != null) ? (String) session.getAttribute("email") : null;
    }

    @Override
    public void destroy() {
        if (noteService != null) {
            noteService.closeDatabases();
        }
    }
}