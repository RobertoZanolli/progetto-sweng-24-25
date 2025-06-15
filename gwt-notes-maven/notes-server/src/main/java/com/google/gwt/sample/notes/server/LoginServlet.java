package com.google.gwt.sample.notes.server;

import com.google.gwt.sample.notes.shared.User;
import javax.servlet.http.*;
import java.io.*;
import java.io.File;

/**
 * Servlet per l'autenticazione degli utenti.
 * Gestisce le richieste di login e la creazione delle sessioni.
 */
public class LoginServlet extends HttpServlet {
    private String dbPath = null;
    private LoginService loginService;

    public LoginServlet() {}
    public LoginServlet(String dbPath) { this.dbPath = dbPath; }

    @Override
    public void init() {
        String pathToUse = dbPath != null ? dbPath : new java.io.File("users.db").getAbsolutePath();
        this.loginService = new LoginService(new File(pathToUse));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user;
        try {
            String json = req.getReader().lines().reduce("", (accumulator, actual) -> accumulator + actual);
            user = UserFactory.fromJson(json);
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Dati utente non validi");
            return;
        }
        if (user == null || user.getEmail() == null || user.getPassword() == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Email e password richieste");
            return;
        }

        try {
            loginService.authenticate(user);
            HttpSession session = req.getSession(true);
            session.setAttribute("email", user.getEmail());

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("Login effettuato con successo");
        } catch (ServiceException e) {
            resp.setStatus(e.getStatusCode());
            resp.getWriter().write(e.getMessage());
        }
    }

    @Override
    public void destroy() {
        if (loginService != null) {
            loginService.close();
        }
    }
}