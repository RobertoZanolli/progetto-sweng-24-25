package com.google.gwt.sample.notes.server;

import com.google.gwt.sample.notes.shared.Note;
import com.google.gwt.sample.notes.shared.Permission;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mapdb.HTreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import java.io.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class HideNoteServletTest {
    private HideNoteServlet servlet;
    private File tempDbFileNote;
/*     private final Session session = Session.getInstance();
 */    private NoteDB noteDB;
    private HTreeMap<String, Note> noteMap;

    @Before
    public void setUp() throws IOException {
        // Crea file temporanei per i due database
        tempDbFileNote = File.createTempFile("notesTest", ".db");
        if (tempDbFileNote.exists()) tempDbFileNote.delete();

        // Istanzia HideNoteServlet con entrambi i database
        servlet = new HideNoteServlet(tempDbFileNote);
        servlet.init();

        noteDB = NoteDB.getInstance(tempDbFileNote);
        noteMap = noteDB.getMap();
    }

    @After
    public void tearDown() {
        if (servlet != null) servlet.destroy();
        NoteDB.resetInstance();
        if (tempDbFileNote != null && tempDbFileNote.exists()) tempDbFileNote.delete();
/*         session.destroy();
 */    }

    // Stub per simulare HttpServletRequest con body e parametro "id"
    private static class StubHttpServletRequest extends HttpServletRequestWrapper {
        private final BufferedReader reader;
        private final String idParam;
        public StubHttpServletRequest(String body, String idParam) {
            super(mock(HttpServletRequest.class));
            this.reader = new BufferedReader(new StringReader(body));
            this.idParam = idParam;
        }
        @Override public BufferedReader getReader() { return reader; }
        @Override public String getParameter(String name) {
            if ("id".equals(name)) return idParam;
            return null;
        }
    }

    // Stub per catturare status e output di HttpServletResponse
    private static class StubHttpServletResponse extends HttpServletResponseWrapper {
        private final StringWriter sw = new StringWriter();
        private final PrintWriter pw = new PrintWriter(sw);
        private int status = 0;
        public StubHttpServletResponse() {
            super(mock(HttpServletResponse.class));
        }
        @Override public PrintWriter getWriter() { return pw; }
        @Override public void setStatus(int sc) { this.status = sc; }
        @Override public void sendError(int sc) throws IOException { this.status = sc; }
        @Override public void sendError(int sc, String msg) throws IOException { this.status = sc; }
        public int getStatus() { return status; }
        public String getOutput() { pw.flush(); return sw.toString(); }
    }

    @Test
    public void testHideNonExistingNote() throws Exception {
/*         session.setUserEmail("user@example.com");
 */        StubHttpServletRequest req = new StubHttpServletRequest("true", "nonexistent");
        StubHttpServletResponse resp = new StubHttpServletResponse();
        servlet.doPut(req, resp);
        assertEquals(HttpServletResponse.SC_NOT_FOUND, resp.getStatus());
        assertTrue(resp.getOutput().contains("Note not found"));
    }

    @Test
    public void testOwnerCannotHide() throws Exception {
        Note note = NoteFactory.create(
            "ownerHideId",
            "Title",
            "Content",
            new String[]{},
            "user@example.com",
            Permission.READ
        );
        noteMap.put(note.getId(), note);
        noteDB.commit();

/*         session.setUserEmail("user@example.com");
 */        StubHttpServletRequest req = new StubHttpServletRequest("true", note.getId());
        StubHttpServletResponse resp = new StubHttpServletResponse();
        servlet.doPut(req, resp);

        assertEquals(HttpServletResponse.SC_BAD_REQUEST, resp.getStatus());
        assertTrue(resp.getOutput().contains("Owner cannot hide note"));
    }

    @Test
    public void testHideMissingBody() throws Exception {
        Note note = NoteFactory.create(
            "missingBodyId",
            "Title",
            "Content",
            new String[]{},
            "owner@example.com",
            Permission.READ
        );
        noteMap.put(note.getId(), note);
        noteDB.commit();

/*         session.setUserEmail("other@example.com");
 */        StubHttpServletRequest req = new StubHttpServletRequest("", note.getId());
        StubHttpServletResponse resp = new StubHttpServletResponse();
        servlet.doPut(req, resp);

        assertEquals(HttpServletResponse.SC_BAD_REQUEST, resp.getStatus());
        assertTrue(resp.getOutput().contains("Missing 'hide' value"));
    }

    @Test
    public void testHideInvalidBody() throws Exception {
        Note note = NoteFactory.create(
            "invalidBodyId",
            "Title",
            "Content",
            new String[]{},
            "owner@example.com",
            Permission.READ
        );
        noteMap.put(note.getId(), note);
        noteDB.commit();

        // Invia un body non booleano
/*         session.setUserEmail("reader@example.com");
 */        StubHttpServletRequest req = new StubHttpServletRequest("nonboolean", note.getId());
        StubHttpServletResponse resp = new StubHttpServletResponse();
        servlet.doPut(req, resp);

        assertEquals(HttpServletResponse.SC_BAD_REQUEST, resp.getStatus());
        assertTrue(resp.getOutput().contains("Invalid 'hide' value"));
    }

    @Test
    public void testHideSuccess() throws Exception {
        Note note = NoteFactory.create(
            "successHideId",
            "Title",
            "Content",
            new String[]{},
            "owner@example.com",
            Permission.READ
        );
        noteMap.put(note.getId(), note);
        noteDB.commit();

/*         session.setUserEmail("reader@example.com");
 */        StubHttpServletRequest req = new StubHttpServletRequest("true", note.getId());
        StubHttpServletResponse resp = new StubHttpServletResponse();
        servlet.doPut(req, resp);

        assertEquals(HttpServletResponse.SC_OK, resp.getStatus());
        assertTrue(resp.getOutput().contains("Note hidden for user"));

        Note updated = noteMap.get(note.getId());
        assertTrue(updated.isHiddenForUser("reader@example.com"));
    }
}