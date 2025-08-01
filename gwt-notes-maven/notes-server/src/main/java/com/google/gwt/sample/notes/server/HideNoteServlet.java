package com.google.gwt.sample.notes.server;

import javax.servlet.http.*;
import javax.servlet.http.HttpServlet;
import java.io.*;

import java.io.File;

/**
 * Servlet per gestire le richieste di nascondere/mostrare note.
 * Gestisce le richieste PUT per modificare la visibilità delle note.
 */
public class HideNoteServlet extends HttpServlet {
    private File dbFileNote = null;
    private final String noteTableName = "notes";    
    private HideNoteService hideNoteService;

    public HideNoteServlet() {}

    public HideNoteServlet(File dbFileNote) {
        this.dbFileNote = dbFileNote;
    }

    public void setDbFileNote(File dbFile) {
        this.dbFileNote = dbFile;
    }

    @Override
    public void init() {
        this.dbFileNote = dbFileNote != null ? dbFileNote : new File(noteTableName + ".db");
        this.hideNoteService = new HideNoteService(this.dbFileNote);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession();
        String userEmail = session.getAttribute("email") != null
            ? (String) session.getAttribute("email")
            : null;

        String noteId = req.getParameter("id");
        // Legge il flag 'hide' dal body
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
            resp.getWriter().write("Valore 'hide' mancante");
            return;
        }
        boolean hide;
        if ("true".equalsIgnoreCase(body)) {
            hide = true;
        } else if ("false".equalsIgnoreCase(body)) {
            hide = false;
        } else {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Valore 'hide' non valido");
            return;
        }

        try {
            hideNoteService.hideNote(noteId, userEmail, hide);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(hide ? "Nota nascosta per l'utente" : "Nota mostrata per l'utente");
        } catch (ServiceException e) {
            resp.setStatus(e.getStatusCode());
            resp.getWriter().write(e.getMessage());
        }
    }

    @Override
    public void destroy() {
        if (hideNoteService != null) {
            hideNoteService.close();
        }
    }
}