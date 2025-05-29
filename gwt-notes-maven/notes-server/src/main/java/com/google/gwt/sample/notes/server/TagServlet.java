package com.google.gwt.sample.notes.server;

import com.google.gwt.sample.notes.shared.Tag;
import com.google.gson.Gson;
import org.mapdb.HTreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.*;

public class TagServlet extends HttpServlet {
    private static final Gson gson = new Gson();
    private File dbFile = null;
    private final String tagTableName = "tags";
    private final String tagLogName = "Tag";
    private TagDB tagDB;

    public TagServlet() {
        // Default constructor for servlet container
    }

    public TagServlet(File dbFile) {
        this.dbFile = dbFile;
    }

    public void setDbFile(File dbFile) {
        this.dbFile = dbFile;
    }

    @Override
    public void init() {
        this.dbFile = dbFile != null ? dbFile : new File(tagTableName + ".db");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        this.tagDB = TagDB.getInstance(this.dbFile);
        HTreeMap<String, Tag> tagMap = tagDB.getMap();
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
        this.tagDB.commit();
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write(tagLogName + " created");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.tagDB = TagDB.getInstance(this.dbFile);
        HTreeMap<String, Tag> tagMap = tagDB.getMap();
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
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    public void destroy() {
        if (tagDB != null) {
            tagDB.close();
        }
    }
}