package com.google.gwt.sample.notes.server;

import com.google.gwt.sample.notes.shared.Tag;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.*;
import java.util.Set;

public class TagServlet extends HttpServlet {
    private File dbFile = null;
    private final String tagTableName = "tags";
    private final String tagLogName = "Tag";
    private TagService tagService;

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
        this.tagService = new TagService(this.dbFile);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Tag tag = null;
        try {
            String json = req.getReader().lines().reduce("", (accumulator, actual) -> accumulator + actual);
            tag = TagFactory.fromJson(json);
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
        try {
            tagService.createTag(tag);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(tagLogName + " created");
        } catch (ServiceException e) {
            resp.setStatus(e.getStatusCode());
            resp.getWriter().write(e.getMessage());
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Set<String> tags;
        try {
            tags = tagService.getAllTags();
        } catch (ServiceException e) {
            resp.setStatus(e.getStatusCode());
            resp.getWriter().write(e.getMessage());
            return;
        }
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        String json = new Gson().toJson(tags.toArray(new String[0]));
        PrintWriter out = resp.getWriter();
        out.print(json);
        out.flush();
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    public void destroy() {
        if (tagService != null) {
            tagService.close();
        }
    }
}