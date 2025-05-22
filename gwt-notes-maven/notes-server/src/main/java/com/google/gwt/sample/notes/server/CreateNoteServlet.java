package com.google.gwt.sample.notes.server;

import com.google.gwt.sample.notes.shared.Note;
import com.google.gwt.sample.notes.shared.Tag;
import com.google.gwt.sample.notes.shared.User;
import com.google.gson.Gson;
import com.password4j.Password;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.mapdb.BTreeMapJava.KeySet;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;

public class CreateNoteServlet extends HttpServlet {
    private DB db;
    HTreeMap<String, Note> noteMap;
    HTreeMap<String, Tag> tagMap;
    private static final Gson gson = new Gson();
    private String dbPathNote = null;
    private String dbPathTag = null;
    private final String noteTableName = "notes";
    private final String noteLogName = "Note";
    private final String tagTableName = "tags";
    private final String tagLogName = "Tag";

    public CreateNoteServlet() {
        // Default constructor for servlet container
    }

    public CreateNoteServlet(String dbPathNote, String dbPathTag) {
        this.dbPathNote = dbPathNote;
        this.dbPathTag = dbPathTag;
    }

    public void setDbPathNote(String dbPath) {
        this.dbPathNote = dbPath;
    }

    public void setDbPathTag(String dbPath) {
        this.dbPathTag = dbPath;
    }

    @Override
    public void init() {
        String pathToUse = dbPathNote != null ? dbPathNote : new java.io.File(noteTableName + ".db").getAbsolutePath();
        try {
            System.out.println("[CreateNoteServlet] Attempting to open DB at: " + pathToUse);
            db = DBMaker.fileDB(pathToUse).make();
            noteMap = db.hashMap(noteTableName, Serializer.STRING, Serializer.JAVA).createOrOpen();

            if (noteMap == null) {
                throw new RuntimeException("Failed to initialize " + noteTableName + " map: " + noteTableName
                        + " is null after createOrOpen() at " + pathToUse);
            }
            System.out.println(
                    "[CreateNoteServlet] DB and " + noteTableName + " map initialized successfully at: " + pathToUse);
        } catch (Exception e) {
            noteMap = null;
            db = null;
            System.err.println("CreateNoteServlet init error: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            try {
                System.err.println("[CreateNoteServlet] Failed DB path: " + pathToUse);
            } catch (Exception ex) {
                System.err.println("[CreateNoteServlet] Could not determine absolute path for " + pathToUse + ": "
                        + ex.getMessage());
            }
        }

        pathToUse = dbPathTag != null ? dbPathTag : new java.io.File(tagTableName + ".db").getAbsolutePath();
        try {
            System.out.println("[CreateNoteServlet] Attempting to open DB at: " + pathToUse);
            db = DBMaker.fileDB(pathToUse).make();
            tagMap = db.hashMap(tagTableName, Serializer.STRING, Serializer.JAVA).createOrOpen();

            if (tagMap == null) {
                throw new RuntimeException("Failed to initialize " + tagTableName + " map: " + tagTableName
                        + " is null after createOrOpen() at " + pathToUse);
            }
            System.out.println(
                    "[CreateNoteServlet] DB and " + tagTableName + " map initialized successfully at: " + pathToUse);
        } catch (Exception e) {
            tagMap = null;
            db = null;
            System.err.println("CreateNoteServlet init error: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            try {
                System.err.println("[CreateNoteServlet] Failed DB path: " + pathToUse);
            } catch (Exception ex) {
                System.err.println("[CreateNoteServlet] Could not determine absolute path for " + pathToUse + ": "
                        + ex.getMessage());
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (noteMap == null) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(noteTableName + " database not initialized");
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
                if (!tagMap.containsKey(tag)) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write("Tag " + tag + " does not exist");
                    return;
                }
            }
        }

        noteMap.put(note.getId(), note);
        db.commit();
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write(noteLogName + " created");
    }

    @Override
    public void destroy() {
        db.close();
    }
}