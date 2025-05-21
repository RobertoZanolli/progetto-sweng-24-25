package com.google.gwt.sample.notes.server;

import com.google.gwt.sample.notes.shared.User;
import com.google.gson.Gson;
import com.password4j.Password;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import javax.servlet.http.*;
import java.io.*;


public class RegisterServlet extends HttpServlet {
    private DB db;
    private HTreeMap<String, String> users;
    private static final Gson gson = new Gson();
    private String dbPath = null;

    public RegisterServlet() {
        // Default constructor for servlet container
    }
    public RegisterServlet(String dbPath) {
        this.dbPath = dbPath;
    }
    public void setDbPath(String dbPath) {
        this.dbPath = dbPath;
    }

    @Override
    public void init() {
        try {
            String pathToUse = dbPath != null ? dbPath : new java.io.File("users.db").getAbsolutePath();
            System.out.println("[RegisterServlet] Attempting to open DB at: " + pathToUse);
            db = DBMaker.fileDB(pathToUse).make();
            users = (HTreeMap<String, String>) db.hashMap("users").createOrOpen();
            if (users == null) {
                throw new RuntimeException("Failed to initialize users map: users is null after createOrOpen() at " + pathToUse);
            }
            System.out.println("[RegisterServlet] DB and users map initialized successfully at: " + pathToUse);
        } catch (Exception e) {
            users = null;
            db = null;
            System.err.println("RegisterServlet init error: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            try {
                String pathToUse = dbPath != null ? dbPath : new java.io.File("users.db").getAbsolutePath();
                System.err.println("[RegisterServlet] Failed DB path: " + pathToUse);
            } catch (Exception ex) {
                System.err.println("[RegisterServlet] Could not determine absolute path for users.db: " + ex.getMessage());
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (users == null) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("User database not initialized");
            return;
        }
        User user = null;
        try {
            user = gson.fromJson(req.getReader(), User.class);
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Invalid user data: " + e.getMessage());
            return;
        }
        if (user == null || user.getEmail() == null || user.getEmail().isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("email required");
            return;
        }
        if (users.containsKey(user.getEmail())) {
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            resp.getWriter().write("User already exists");
            return;
        }
        String hash = hashPassword(user.getPasswordHash());
        users.put(user.getEmail(), hash);
        db.commit();
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write("User registered");
    }

    private String hashPassword(String password) {
        return Password.hash(password).withBcrypt().getResult();
    }
}