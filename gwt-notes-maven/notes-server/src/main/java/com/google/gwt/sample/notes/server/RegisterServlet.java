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
    private UserDB userDB;

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
        userDB = UserDB.getInstance(dbPath);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HTreeMap<String, String> users = userDB.getUsers();
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
        if (!user.getEmail().contains("@")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("invalid email");
            return;
        }
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("password required");
            return;
        }
        if (users.containsKey(user.getEmail())) {
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            resp.getWriter().write("User already exists");
            return;
        }
        String hash = hashPassword(user.getPassword());
        users.put(user.getEmail(), hash);
        userDB.commit();
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write("User registered");
    }

    private String hashPassword(String password) {
        return Password.hash(password).withBcrypt().getResult();
    }
}