package com.google.gwt.sample.notes.server;

import javax.servlet.http.*;
import java.io.IOException;

public class SessionServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("Authenticated as " + session.getAttribute("user"));
        } else {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("Not authenticated");
        }
    }
}