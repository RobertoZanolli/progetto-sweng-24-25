package com.google.gwt.sample.notes.server;

import com.google.gwt.sample.notes.shared.ConcreteTag;
import com.google.gwt.sample.notes.shared.Tag;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.*;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class TagServletTest {
    private TagServlet servlet;
    private final String tableName = "tagsTest";
    private final String logName = "Tag";
    private File tempFile;

    @Before
    public void setUp() throws IOException {
        tempFile = File.createTempFile(tableName, ".db");
        if (tempFile.exists()) tempFile.delete();
        servlet = new TagServlet(tempFile);
        servlet.init();
        TagDB tagDB = TagDB.getInstance(tempFile);
        assertNotNull(tagDB);
        assertNotNull(tagDB.getMap());
    }

    @After
    public void tearDown() {
        if (servlet != null) servlet.destroy();
        TagDB.resetInstance();
        if (tempFile != null && tempFile.exists()) tempFile.delete();
    }

    // --- Stub classes ---
    private static class StubHttpServletRequest extends HttpServletRequestWrapper {
        private final BufferedReader reader;
        private final String method;
        private final String nameParam;
        public StubHttpServletRequest(String body) {
            super(mock(HttpServletRequest.class));
            this.reader = new BufferedReader(new StringReader(body));
            this.method = "POST";
            this.nameParam = null;
        }
        public StubHttpServletRequest(String body, String method, String nameParam) {
            super(mock(HttpServletRequest.class));
            this.reader = new BufferedReader(new StringReader(body));
            this.method = method;
            this.nameParam = nameParam;
        }
        @Override public BufferedReader getReader() { return reader; }
        @Override public String getMethod() { return method; }
        @Override public String getParameter(String name) {
            if ("name".equals(name)) return nameParam;
            return null;
        }
    }
    private static class StubHttpServletResponse extends HttpServletResponseWrapper {
        private final StringWriter sw = new StringWriter();
        private final PrintWriter pw = new PrintWriter(sw);
        private int status = 0;
        public StubHttpServletResponse() { super(mock(HttpServletResponse.class)); }
        @Override public PrintWriter getWriter() { return pw; }
        @Override public void setStatus(int sc) { this.status = sc; }
        public int getStatus() { return status; }
        public String getOutput() { pw.flush(); return sw.toString(); }
        @Override public void sendError(int sc) throws IOException { this.status = sc; }
        @Override public void sendError(int sc, String msg) throws IOException { this.status = sc; }
    }

    // --- Helper for tag creation ---
    private Tag createValidTag(String name) {
        return new ConcreteTag(name);
    }

    // --- Tests ---

    @Test
    public void testCreateNewTag() throws Exception {
        Tag tag = createValidTag("nuovo tag");
        String json = TagFactory.toJson(tag);
        StubHttpServletRequest req = new StubHttpServletRequest(json);
        StubHttpServletResponse resp = new StubHttpServletResponse();
        servlet.doPost(req, resp);
        assertEquals(HttpServletResponse.SC_OK, resp.getStatus());
        assertTrue(resp.getOutput().contains(logName + " created"));
    }

    @Test
    public void testCreateDuplicateTag() throws Exception {
        Tag tag = createValidTag("dupTag");
        String json = TagFactory.toJson(tag);
        // First creation should succeed
        StubHttpServletRequest req1 = new StubHttpServletRequest(json);
        StubHttpServletResponse resp1 = new StubHttpServletResponse();
        servlet.doPost(req1, resp1);
        assertEquals(HttpServletResponse.SC_OK, resp1.getStatus());
        assertTrue(resp1.getOutput().contains(logName + " created"));
        // Second creation (duplicate) should fail with conflict
        StubHttpServletRequest req2 = new StubHttpServletRequest(json);
        StubHttpServletResponse resp2 = new StubHttpServletResponse();
        servlet.doPost(req2, resp2);
        assertEquals(HttpServletResponse.SC_CONFLICT, resp2.getStatus());
        assertTrue(resp2.getOutput().contains(logName + " already exists"));
    }

    @Test
    public void testCreateTagWithInvalidJson() throws Exception {
        StubHttpServletRequest req = mock(StubHttpServletRequest.class);
        StubHttpServletResponse resp = new StubHttpServletResponse();
        when(req.getReader()).thenThrow(new IOException("Simulated IO error"));
        servlet.doPost(req, resp);
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, resp.getStatus());
        assertTrue(resp.getOutput().contains("Invalid " + logName + " data"));
    }

    @Test
    public void testCreateTagWithInvalidName() throws Exception {
        Tag tag1 = new ConcreteTag();
        Tag tag2 = new ConcreteTag("");
        for (Tag tag : new Tag[] { tag1, tag2 }) {
            String json = TagFactory.toJson(tag);
            StubHttpServletRequest req = new StubHttpServletRequest(json);
            StubHttpServletResponse resp = new StubHttpServletResponse();
            servlet.doPost(req, resp);
            assertEquals(HttpServletResponse.SC_BAD_REQUEST, resp.getStatus());
            assertTrue(resp.getOutput().contains("Name required"));
        }
    }

    @Test
    public void testGetTags() throws Exception {
        Tag tag = createValidTag("nuovo tag");
        String json = TagFactory.toJson(tag);
        // Create the tag
        StubHttpServletRequest postReq = new StubHttpServletRequest(json);
        StubHttpServletResponse postResp = new StubHttpServletResponse();
        servlet.doPost(postReq, postResp);
        assertEquals(HttpServletResponse.SC_OK, postResp.getStatus());
        // Test GET
        StubHttpServletRequest getReq = new StubHttpServletRequest("");
        StubHttpServletResponse getResp = new StubHttpServletResponse();
        servlet.doGet(getReq, getResp);
        assertEquals(HttpServletResponse.SC_OK, getResp.getStatus());
        String output = getResp.getOutput();
        assertTrue(output.contains("nuovo tag"));
    }
}