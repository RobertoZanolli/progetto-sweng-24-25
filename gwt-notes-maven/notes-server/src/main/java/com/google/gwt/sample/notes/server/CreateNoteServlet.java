package com.google.gwt.sample.notes.server;

import com.google.gwt.sample.notes.shared.Note;
import com.google.gwt.sample.notes.shared.User;
import com.google.gson.Gson;
import com.password4j.Password;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.mapdb.BTreeMapJava.KeySet;

import javax.servlet.http.*;
import java.io.*;


public class CreateNoteServlet extends HttpServlet {
    private DB db;
    HTreeMap<String, Note> noteMap; 
    private static final Gson gson = new Gson();
    private String dbPath = null;
    private final String tableName = "notes";

    public CreateNoteServlet() {
        // Default constructor for servlet container
    }
    public CreateNoteServlet(String dbPath) {
        this.dbPath = dbPath;
    }
    public void setDbPath(String dbPath) {
        this.dbPath = dbPath;
    }

    @Override
    public void init() {
        String pathToUse = dbPath != null ? dbPath : new java.io.File(tableName+".db").getAbsolutePath();
        try {
            System.out.println("[CreateNoteServlet] Attempting to open DB at: " + pathToUse);
            db = DBMaker.fileDB(pathToUse).make();
            noteMap = db.hashMap("notes", Serializer.STRING, Serializer.JAVA).createOrOpen();

            if (noteMap == null) {
                throw new RuntimeException("Failed to initialize "+tableName+" map: "+tableName+" is null after createOrOpen() at " + pathToUse);
            }
            System.out.println("[CreateNoteServlet] DB and "+tableName+" map initialized successfully at: " + pathToUse);
        } catch (Exception e) {
            noteMap = null;
            db = null;
            System.err.println("CreateNoteServlet init error: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            try {
                System.err.println("[CreateNoteServlet] Failed DB path: " + pathToUse);
            } catch (Exception ex) {
                System.err.println("[CreateNoteServlet] Could not determine absolute path for "+pathToUse+": " + ex.getMessage());
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (noteMap == null) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(tableName+" database not initialized");
            return;
        }
        // correggi subito!!!!!!!
        int userId = 0;
        userId = userId==0 ? 1 : userId++;
        Note note = null;
        try {
            String strNote = req.getReader().lines().reduce("", (accumulator, actual) -> accumulator + actual);
            note = NoteFactory.fromJson(strNote);
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Invalid "+tableName+" data: " + e.getMessage());
            return;
        }
        if (note == null || note.getTitle() == null || note.getTitle().isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Title required");
            return;
        }
        if (noteMap.containsKey(note.getId())) {
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            resp.getWriter().write(tableName+" already exists");
            return;
        }

        noteMap.put(note.getId(), note);
        db.commit();
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write("User registered");
    }
}