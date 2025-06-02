package com.google.gwt.sample.notes.server;

import javax.servlet.http.*;
import java.io.IOException;

public class SessionServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("userEmail") != null) {
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("Authenticated as " + session.getAttribute("userEmail"));
        } else {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("Not authenticated");
        }
    }
}