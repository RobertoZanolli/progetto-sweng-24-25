package com.google.gwt.sample.notes.server;

import com.google.gwt.sample.notes.shared.User;

import javax.servlet.http.*;
import java.io.*;
import java.io.File;

public class RegisterServlet extends HttpServlet {
    private String dbPath = null;
    private RegisterService registerService;

    public RegisterServlet() {
        // Default constructor for servlet container
    }
    public RegisterServlet(String dbPath) {
        this.dbPath = dbPath;
    }

    @Override
    public void init() {
        String pathToUse = dbPath != null ? dbPath : new java.io.File("users.db").getAbsolutePath();
        this.registerService = new RegisterService(new File(pathToUse));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = null;
        try {
            String json = req.getReader().lines().reduce("", (accumulator, actual) -> accumulator + actual);
            user = UserFactory.fromJson(json);
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Invalid user data");
            return;
        }

        try {
            registerService.register(user);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("User registered");
        } catch (ServiceException e) {
            resp.setStatus(e.getStatusCode());
            resp.getWriter().write(e.getMessage());
        }
    }
    
    @Override
    public void destroy() {
        if (registerService != null) {
            registerService.close();
        }
    }
}