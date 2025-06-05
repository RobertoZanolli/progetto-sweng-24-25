package com.google.gwt.sample.notes.server;

import com.google.gson.Gson;
import com.google.gwt.sample.notes.shared.Session;
import com.google.gwt.sample.notes.shared.User;
import com.password4j.Password;
import org.mapdb.HTreeMap;

import javax.servlet.http.*;
import java.io.*;

public class LoginServlet extends HttpServlet {
    private static final Gson gson = new Gson();
    private String dbPath = null;
    private UserDB userDB;
    private Session session;

    public LoginServlet() {}
    public LoginServlet(String dbPath) { this.dbPath = dbPath; }

    @Override
    public void init() {
        String pathToUse = dbPath != null ? dbPath : new java.io.File("users.db").getAbsolutePath();
        userDB = UserDB.getInstance(new File(pathToUse));
        session = Session.getInstance();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HTreeMap<String, String> users = userDB.getMap();
        if (users == null) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("User database not initialized");
            return;
        }
        User user;
        try {
            user = gson.fromJson(req.getReader(), User.class);
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Invalid user data");
            return;
        }
        if (user == null || user.getEmail() == null || user.getPassword() == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Email and password required");
            return;
        }
        String hash = users.get(user.getEmail());
        if (hash == null || !Password.check(user.getPassword(), hash).withBcrypt()) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("Invalid credentials");
            return;
        }

        session.setUserEmail(user.getEmail());
        
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write("Login successful");
    }
}