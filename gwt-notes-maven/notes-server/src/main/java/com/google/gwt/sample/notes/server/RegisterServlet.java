package com.google.gwt.sample.notes.server;

import com.google.gwt.sample.notes.shared.User;
import com.password4j.Password;
import org.mapdb.HTreeMap;

import javax.servlet.http.*;
import java.io.*;

public class RegisterServlet extends HttpServlet {
    private String dbPath = null;
    private UserDB userDB;

    public RegisterServlet() {
        // Default constructor for servlet container
    }
    public RegisterServlet(String dbPath) {
        this.dbPath = dbPath;
    }

    @Override
    public void init() {
        String pathToUse = dbPath != null ? dbPath : new java.io.File("users.db").getAbsolutePath();
        userDB = UserDB.getInstance(new File(pathToUse));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HTreeMap<String, String> users = userDB.getMap();
        if (users == null) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("User database not initialized");
            return;
        }

        User user = null;
        try {
            String json = req.getReader().lines().reduce("", (accumulator, actual) -> accumulator + actual);
            user = UserFactory.fromJson(json);
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Invalid user data");
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

        String hash = Password.hash(user.getPassword()).withBcrypt().getResult();
        users.put(user.getEmail(), hash);
        userDB.commit();
        
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write("User registered");
    }
}