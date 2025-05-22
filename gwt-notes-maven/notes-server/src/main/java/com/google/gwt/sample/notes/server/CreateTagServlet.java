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

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;

public class CreateTagServlet extends HttpServlet {
    private DB db;
    HTreeMap<String, Tag> tagMap;
    private static final Gson gson = new Gson();
    private String dbPath = null;
    private final String tagTableName = "tags";
    private final String tagLogName = "Tag";

    public CreateTagServlet() {
        // Default constructor for servlet container
    }

    public CreateTagServlet(String dbPath) {
        this.dbPath = dbPath;
    }

    public void setDbPath(String dbPath) {
        this.dbPath = dbPath;
    }

    @Override
    public void init() {
        String pathToUse = dbPath != null ? dbPath : new java.io.File(tagTableName + ".db").getAbsolutePath();
        try {
            System.out.println("[CreateTagServlet] Attempting to open DB at: " + pathToUse);
            db = DBMaker.fileDB(pathToUse).make();
            tagMap = db.hashMap(tagTableName, Serializer.STRING, Serializer.JAVA).createOrOpen();

            if (tagMap == null) {
                throw new RuntimeException("Failed to initialize " + tagTableName + " map: " + tagTableName
                        + " is null after createOrOpen() at " + pathToUse);
            }
            System.out.println(
                    "[CreateTagServlet] DB and " + tagTableName + " map initialized successfully at: " + pathToUse);
        } catch (Exception e) {
            tagMap = null;
            db = null;
            System.err.println("CreateTagServlet init error: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            try {
                System.err.println("[CreateTagServlet] Failed DB path: " + pathToUse);
            } catch (Exception ex) {
                System.err.println("[CreateTagServlet] Could not determine absolute path for " + pathToUse + ": "
                        + ex.getMessage());
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (tagMap == null) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(tagTableName + " database not initialized");
            return;
        }

        Tag tag = null;
        try {
            tag = gson.fromJson(req.getReader(), Tag.class);
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Invalid " + tagLogName + " data: " + e.getMessage());
            return;
        }
        if (tag == null || tag.getName() == null || tag.getName().isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Name required");
            return;
        }
        if (tagMap.containsKey(tag.getName())) {
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            resp.getWriter().write(tagLogName + " already exists");
            return;
        }

        tagMap.put(tag.getName(), tag);
        db.commit();
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write(tagLogName + " created");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (tagMap == null) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(tagTableName + " database not initialized");
            return;
        }

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        // Converti il Set in array di stringhe
        String[] tagsArray = tagMap.keySet().toArray(new String[0]);

        // Usa Gson per serializzare l'array in JSON
        String json = new Gson().toJson(tagsArray);

        PrintWriter out = resp.getWriter();
        out.print(json);
        out.flush();
    }

        @Override
    public void destroy() {
        db.close();
    }
}