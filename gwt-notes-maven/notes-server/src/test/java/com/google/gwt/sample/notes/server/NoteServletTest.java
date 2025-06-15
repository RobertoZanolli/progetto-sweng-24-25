package com.google.gwt.sample.notes.server;

import com.google.gwt.sample.notes.shared.Note;
import com.google.gwt.sample.notes.shared.ConcreteTag;
import com.google.gwt.sample.notes.shared.ConcreteVersion;
import com.google.gwt.sample.notes.shared.Permission;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mapdb.HTreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.HttpSession;

import java.io.*;
import java.util.Date;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class NoteServletTest {
    private NoteServlet servlet;
    private File tempDbFileNote;
    private File tempDbFileTag;
    private final String noteTableName = "notesTest";
    private final String noteLogName = "Note";
    private final String tagTableName = "tagsTest";
    private final String tagLogName = "Tag";

    @Before
    public void setUp() throws IOException {
        tempDbFileNote = File.createTempFile(noteTableName, ".db");
        if (tempDbFileNote.exists())
            tempDbFileNote.delete();
        tempDbFileTag = File.createTempFile(tagTableName, ".db");
        if (tempDbFileTag.exists())
            tempDbFileTag.delete();
        servlet = new NoteServlet(tempDbFileNote, tempDbFileTag);
        servlet.init();
        TagDB tagDB = TagDB.getInstance(tempDbFileTag);
        NoteDB noteDB = NoteDB.getInstance(tempDbFileNote);
        assertNotNull(tagDB);
        assertNotNull(noteDB);
        assertNotNull(noteDB.getMap());
        assertNotNull(tagDB.getMap());
        tagDB.getMap().put("testTag", new ConcreteTag("testTag"));
        tagDB.commit();
    }

    @After
    public void tearDown() {
        if (servlet != null)
            servlet.destroy();
        NoteDB.resetInstance();
        TagDB.resetInstance();
        if (tempDbFileNote != null && tempDbFileNote.exists())
            tempDbFileNote.delete();
        if (tempDbFileTag != null && tempDbFileTag.exists())
            tempDbFileTag.delete();
    }

    private static class StubHttpServletRequest extends HttpServletRequestWrapper {
        private final BufferedReader reader;
        private final String method;
        private final String idParam;
        private final String permissionParam;
        private final HttpSession session;

        public StubHttpServletRequest(String body) {
            super(mock(HttpServletRequest.class));
            this.reader = new BufferedReader(new StringReader(body));
            this.method = "POST";
            this.idParam = null;
            this.permissionParam = null;
            this.session = mock(HttpSession.class);
        }

        public StubHttpServletRequest(String body, String method, String idParam, String permissionParam) {
            super(mock(HttpServletRequest.class));
            this.reader = new BufferedReader(new StringReader(body));
            this.method = method;
            this.idParam = idParam;
            this.permissionParam = permissionParam;
            this.session = mock(HttpSession.class);
        }

        public StubHttpServletRequest(String body, String method, String idParam, String permissionParam, HttpSession session) {
            super(mock(HttpServletRequest.class));
            this.reader = new BufferedReader(new StringReader(body));
            this.method = method;
            this.idParam = idParam;
            this.permissionParam = permissionParam;
            this.session = session;
        }

        @Override
        public BufferedReader getReader() {
            return reader;
        }

        @Override
        public String getMethod() {
            return method;
        }

        @Override
        public String getParameter(String name) {
            if ("id".equals(name))
                return idParam;
            if ("permission".equals(name))
                return permissionParam;
            if ("hide".equals(name))
                return permissionParam;
            return null;
        }

        @Override
        public HttpSession getSession() {
            return session;
        }

        @Override
        public HttpSession getSession(boolean create) {
            return session;
        }
    }

    private static class StubHttpServletResponse extends HttpServletResponseWrapper {
        private final StringWriter sw = new StringWriter();
        private final PrintWriter pw = new PrintWriter(sw);
        private int status = 0;

        public StubHttpServletResponse() {
            super(mock(HttpServletResponse.class));
        }

        @Override
        public PrintWriter getWriter() {
            return pw;
        }

        @Override
        public void setStatus(int sc) {
            this.status = sc;
        }

        public int getStatus() {
            return status;
        }

        public String getOutput() {
            pw.flush();
            return sw.toString();
        }

        @Override
        public void sendError(int sc) throws IOException {
            this.status = sc;
        }

        @Override
        public void sendError(int sc, String msg) throws IOException {
            this.status = sc;
        }
    }

    private Note createValidNote(String id) {
        String[] tags = new String[] { "testTag" };
        Note note = NoteFactory.create(id, "Test Title", "Test Content", tags, "user@example.com", Permission.PRIVATE);
        return note;
    }

    @Test
    public void testCreateNewNote() throws Exception {
        Note note = createValidNote("testTag2");
        String json = NoteFactory.toJson(note);

        String ownerEmail = note.getOwnerEmail();
        HttpSession mockSession = mock(HttpSession.class);
        when(mockSession.getAttribute("email")).thenReturn(ownerEmail);

        StubHttpServletRequest req = new StubHttpServletRequest(json, "POST", null, null, mockSession);
        StubHttpServletResponse resp = new StubHttpServletResponse();
        servlet.doPost(req, resp);
        assertEquals(HttpServletResponse.SC_OK, resp.getStatus());
        assertTrue(resp.getOutput().contains(noteLogName + " created"));
    }

    @Test
    public void testCreateDuplicateNote() throws Exception {
        Note note = createValidNote("dupId");
        String json = NoteFactory.toJson(note);

        String ownerEmail = note.getOwnerEmail();
        HttpSession mockSession = mock(HttpSession.class);
        when(mockSession.getAttribute("email")).thenReturn(ownerEmail);

        StubHttpServletRequest req1 = new StubHttpServletRequest(json, "POST", null, null, mockSession);
        StubHttpServletResponse resp1 = new StubHttpServletResponse();
        servlet.doPost(req1, resp1);
        assertEquals(HttpServletResponse.SC_OK, resp1.getStatus());

        StubHttpServletRequest req2 = new StubHttpServletRequest(json, "POST", null, null, mockSession);
        StubHttpServletResponse resp2 = new StubHttpServletResponse();
        servlet.doPost(req2, resp2);
        assertEquals(HttpServletResponse.SC_CONFLICT, resp2.getStatus());
        assertTrue(resp2.getOutput().contains(noteLogName + " already exists"));
    }

    @Test
    public void testCreateNoteWithEmptyTitle() throws Exception {
        Note note = createValidNote("emptyTitleId");
        note.getCurrentVersion().setTitle("");
        String json = NoteFactory.toJson(note);

        String ownerEmail = note.getOwnerEmail();
        HttpSession mockSession = mock(HttpSession.class);
        when(mockSession.getAttribute("email")).thenReturn(ownerEmail);

        StubHttpServletRequest req = new StubHttpServletRequest(json, "POST", null, null, mockSession);
        StubHttpServletResponse resp = new StubHttpServletResponse();
        servlet.doPost(req, resp);
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, resp.getStatus());
        assertTrue(resp.getOutput().contains("Title required"));
    }

    @Test
    public void testCreateNoteWithNullTags() throws Exception {
        Note note = createValidNote("nullTagId");
        note.setTags(new String[] { null });
        String json = NoteFactory.toJson(note);

        String ownerEmail = note.getOwnerEmail();
        HttpSession mockSession = mock(HttpSession.class);
        when(mockSession.getAttribute("email")).thenReturn(ownerEmail);

        StubHttpServletRequest req = new StubHttpServletRequest(json, "POST", null, null, mockSession);
        StubHttpServletResponse resp = new StubHttpServletResponse();
        servlet.doPost(req, resp);
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, resp.getStatus());
        assertTrue(resp.getOutput().contains(tagLogName + " name required"));
    }

    @Test
    public void testCreateNoteWithNonexistentTag() throws Exception {
        Note note = createValidNote("badTagId");
        note.setTags(new String[] { "notExistTag" });
        String json = NoteFactory.toJson(note);

        String ownerEmail = note.getOwnerEmail();
        HttpSession mockSession = mock(HttpSession.class);
        when(mockSession.getAttribute("email")).thenReturn(ownerEmail);

        StubHttpServletRequest req = new StubHttpServletRequest(json, "POST", null, null, mockSession);
        StubHttpServletResponse resp = new StubHttpServletResponse();
        servlet.doPost(req, resp);
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, resp.getStatus());
        assertTrue(resp.getOutput().contains("Tag notExistTag does not exist"));
    }

    @Test
    public void testCreateNoteWithInvalidJson() throws Exception {
        StubHttpServletRequest req = mock(StubHttpServletRequest.class);
        StubHttpServletResponse resp = new StubHttpServletResponse();
        when(req.getReader()).thenThrow(new IOException("Simulated IO error"));
        servlet.doPost(req, resp);
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, resp.getStatus());
        assertTrue(resp.getOutput().contains("Invalid " + noteLogName + " data"));
    }

    @Test
    public void testGetNotes() throws Exception {
        Note note = createValidNote("getId");
        String json = NoteFactory.toJson(note);

        String ownerEmail = note.getOwnerEmail();
        HttpSession mockSession = mock(HttpSession.class);
        when(mockSession.getAttribute("email")).thenReturn(ownerEmail);

        StubHttpServletRequest postReq = new StubHttpServletRequest(json, "POST", null, null, mockSession);
        StubHttpServletResponse postResp = new StubHttpServletResponse();
        servlet.doPost(postReq, postResp);
        assertEquals(HttpServletResponse.SC_OK, postResp.getStatus());

        StubHttpServletRequest getReq = new StubHttpServletRequest("", "GET", null, null, mockSession);
        StubHttpServletResponse getResp = new StubHttpServletResponse();
        servlet.doGet(getReq, getResp);
        assertEquals(HttpServletResponse.SC_OK, getResp.getStatus());
        String output = getResp.getOutput();
        assertTrue(output.contains("Test Title"));
        assertTrue(output.contains("Test Content"));
    }

    @Test
    public void testDeleteExistingNote() throws Exception {
        Note note = createValidNote("delId");
        String json = NoteFactory.toJson(note);

        String ownerEmail = note.getOwnerEmail();
        HttpSession mockSession = mock(HttpSession.class);
        when(mockSession.getAttribute("email")).thenReturn(ownerEmail);

        StubHttpServletRequest postReq = new StubHttpServletRequest(json, "POST", null, null, mockSession);
        StubHttpServletResponse postResp = new StubHttpServletResponse();
        servlet.doPost(postReq, postResp);
        assertEquals(HttpServletResponse.SC_OK, postResp.getStatus());

        StubHttpServletRequest deleteReq = new StubHttpServletRequest("", "DELETE", note.getId(), null, mockSession);
        StubHttpServletResponse deleteResp = new StubHttpServletResponse();
        servlet.doDelete(deleteReq, deleteResp);

        assertEquals(HttpServletResponse.SC_OK, deleteResp.getStatus());
        assertTrue(deleteResp.getOutput().contains(noteLogName + " with ID " + note.getId() + " deleted"));
    }

    @Test
    public void testDeleteNonExistingNote() throws Exception {
        HttpSession mockSession = mock(HttpSession.class);
        StubHttpServletRequest deleteReq = new StubHttpServletRequest("", "DELETE", "nonexistentId", null, mockSession);
        StubHttpServletResponse deleteResp = new StubHttpServletResponse();
        servlet.doDelete(deleteReq, deleteResp);

        assertEquals(HttpServletResponse.SC_NOT_FOUND, deleteResp.getStatus());
        assertTrue(deleteResp.getOutput().contains(noteLogName + " with ID nonexistentId not found"));
    }

    @Test
    public void testDeleteWithoutId() throws Exception {
        HttpSession mockSession = mock(HttpSession.class);
        StubHttpServletRequest deleteReq = new StubHttpServletRequest("", "DELETE", null, null, mockSession);
        StubHttpServletResponse deleteResp = new StubHttpServletResponse();
        servlet.doDelete(deleteReq, deleteResp);

        assertEquals(HttpServletResponse.SC_BAD_REQUEST, deleteResp.getStatus());
        assertTrue(deleteResp.getOutput().contains("Note ID required"));
    }

    @Test
    public void testCreateNoteWithNullPermission() throws Exception {
        Note note = createValidNote("nullPermId");
        String json = NoteFactory.toJson(note);

        String ownerEmail = note.getOwnerEmail();
        HttpSession mockSession = mock(HttpSession.class);
        when(mockSession.getAttribute("email")).thenReturn(ownerEmail);

        StubHttpServletRequest postReq = new StubHttpServletRequest(json, "POST", null, null, mockSession);
        StubHttpServletResponse postResp = new StubHttpServletResponse();
        servlet.doPost(postReq, postResp);

        assertEquals(HttpServletResponse.SC_OK, postResp.getStatus());

        StubHttpServletRequest getReqOwner = new StubHttpServletRequest("", "GET", null, null, mockSession);
        StubHttpServletResponse getRespOwner = new StubHttpServletResponse();
        servlet.doGet(getReqOwner, getRespOwner);
        assertEquals(HttpServletResponse.SC_OK, getRespOwner.getStatus());
        String outputOwner = getRespOwner.getOutput();
        assertTrue(outputOwner.contains("nullPermId"));

        String otherEmail = "other@example.com";
        HttpSession otherMockSession = mock(HttpSession.class);
        when(otherMockSession.getAttribute("email")).thenReturn(otherEmail);
        StubHttpServletRequest getReqOther = new StubHttpServletRequest("", "GET", null, null, otherMockSession);
        StubHttpServletResponse getRespOther = new StubHttpServletResponse();
        servlet.doGet(getReqOther, getRespOther);
        assertEquals(HttpServletResponse.SC_OK, getRespOther.getStatus());
        String outputOther = getRespOther.getOutput();
        assertFalse(outputOther.contains("nullPermId"));
    }

    @Test
    public void testUpdateNoteWithNewVersionAndTags() throws Exception {
        Note note = createValidNote("putId");
        String json = NoteFactory.toJson(note);

        String ownerEmail = note.getOwnerEmail();
        HttpSession mockSession = mock(HttpSession.class);
        when(mockSession.getAttribute("email")).thenReturn(ownerEmail);

        StubHttpServletRequest postReq = new StubHttpServletRequest(json, "POST", null, null, mockSession);
        StubHttpServletResponse postResp = new StubHttpServletResponse();
        servlet.doPost(postReq, postResp);

        assertEquals(HttpServletResponse.SC_OK, postResp.getStatus());

        ConcreteVersion newVersion = new ConcreteVersion();
        newVersion.setTitle("Updated Title");
        newVersion.setContent("Updated Content");
        newVersion.setUpdatedAt(new Date());
        String putJson = "{\"title\":\"Updated Title\",\"content\":\"Updated Content\",\"updatedAt\":\"2025-05-22T10:06:02Z\",\"tags\":[\"testTag\"]}";
        StubHttpServletRequest putReq = new StubHttpServletRequest(putJson, "PUT", note.getId(), null, mockSession);
        StubHttpServletResponse putResp = new StubHttpServletResponse();
        servlet.doPut(putReq, putResp);

        assertEquals(HttpServletResponse.SC_OK, putResp.getStatus());
        assertTrue(putResp.getOutput().contains("Note updated with new version"));
    }

    @Test
    public void testUpdateNoteWithMissingTitle() throws Exception {
        Note note = createValidNote("putMissingTitleId");
        String json = NoteFactory.toJson(note);

        String ownerEmail = note.getOwnerEmail();
        HttpSession mockSession = mock(HttpSession.class);
        when(mockSession.getAttribute("email")).thenReturn(ownerEmail);

        StubHttpServletRequest postReq = new StubHttpServletRequest(json, "POST", null, null, mockSession);
        StubHttpServletResponse postResp = new StubHttpServletResponse();
        servlet.doPost(postReq, postResp);

        assertEquals(HttpServletResponse.SC_OK, postResp.getStatus());

        String putJson = "{\"title\":\"\",\"content\":\"Updated Content\",\"updatedAt\":\"2025-05-22T10:06:02Z\",\"tags\":[\"testTag\"]}";
        StubHttpServletRequest putReq = new StubHttpServletRequest(putJson, "PUT", note.getId(), null, mockSession);
        StubHttpServletResponse putResp = new StubHttpServletResponse();
        servlet.doPut(putReq, putResp);

        assertEquals(HttpServletResponse.SC_BAD_REQUEST, putResp.getStatus());
        assertTrue(putResp.getOutput().contains("Title required"));
    }

    @Test
    public void testUpdateNoteWithNonexistentTag() throws Exception {
        Note note = createValidNote("putBadTagId");
        String json = NoteFactory.toJson(note);

        String ownerEmail = note.getOwnerEmail();
        HttpSession mockSession = mock(HttpSession.class);
        when(mockSession.getAttribute("email")).thenReturn(ownerEmail);

        StubHttpServletRequest postReq = new StubHttpServletRequest(json, "POST", null, null, mockSession);
        StubHttpServletResponse postResp = new StubHttpServletResponse();
        servlet.doPost(postReq, postResp);

        assertEquals(HttpServletResponse.SC_OK, postResp.getStatus());
        String putJson = "{\"title\":\"Updated Title\",\"content\":\"Updated Content\",\"updatedAt\":\"2025-05-22T10:06:02Z\",\"tags\":[\"notExistTag\"]}";
        StubHttpServletRequest putReq = new StubHttpServletRequest(putJson, "PUT", note.getId(), null, mockSession);
        StubHttpServletResponse putResp = new StubHttpServletResponse();
        servlet.doPut(putReq, putResp);
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, putResp.getStatus());
        assertTrue(putResp.getOutput().contains("Tag notExistTag does not exist"));
    }

    @Test
    public void testUpdateNonExistingNote() throws Exception {
        HttpSession mockSession = mock(HttpSession.class);
        String putJson = "{\"title\":\"Updated Title\",\"content\":\"Updated Content\",\"updatedAt\":\"2025-05-22T10:06:02Z\",\"tags\":[\"testTag\"]}";
        StubHttpServletRequest putReq = new StubHttpServletRequest(putJson, "PUT", "nonexistentId", null, mockSession);
        StubHttpServletResponse putResp = new StubHttpServletResponse();
        servlet.doPut(putReq, putResp);
        assertEquals(HttpServletResponse.SC_NOT_FOUND, putResp.getStatus());
        assertTrue(putResp.getOutput().contains("Note not found."));
    }

    @Test
    public void testPermissionWithOutOwner() throws Exception {
        Note note = createValidNote("privateId");
        note.setPermission(Permission.PRIVATE);
        String json = NoteFactory.toJson(note);

        String ownerEmail = note.getOwnerEmail();
        HttpSession mockSession = mock(HttpSession.class);
        when(mockSession.getAttribute("email")).thenReturn(ownerEmail);

        StubHttpServletRequest postReq = new StubHttpServletRequest(json, "POST", null, null, mockSession);
        StubHttpServletResponse postResp = new StubHttpServletResponse();
        servlet.doPost(postReq, postResp);

        assertEquals(HttpServletResponse.SC_OK, postResp.getStatus());

        HttpSession emptySession = mock(HttpSession.class);
        when(emptySession.getAttribute("email")).thenReturn(null);
        StubHttpServletRequest getReqOwner = new StubHttpServletRequest("", "GET", null, null, emptySession);
        StubHttpServletResponse getRespOwner = new StubHttpServletResponse();
        servlet.doGet(getReqOwner, getRespOwner);
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, getRespOwner.getStatus());
        String outputOwner = getRespOwner.getOutput();
        assertTrue(outputOwner.contains("Utente non autenticato"));
    }

    @Test
    public void testPermissionPrivate() throws Exception {
        Note note = createValidNote("privateId");
        note.setPermission(Permission.PRIVATE);
        String json = NoteFactory.toJson(note);

        String ownerEmail = note.getOwnerEmail();
        HttpSession mockSession = mock(HttpSession.class);
        when(mockSession.getAttribute("email")).thenReturn(ownerEmail);

        StubHttpServletRequest postReq = new StubHttpServletRequest(json, "POST", null, null, mockSession);
        StubHttpServletResponse postResp = new StubHttpServletResponse();
        servlet.doPost(postReq, postResp);

        assertEquals(HttpServletResponse.SC_OK, postResp.getStatus());

        StubHttpServletRequest getReqOwner = new StubHttpServletRequest("", "GET", null, null, mockSession);
        StubHttpServletResponse getRespOwner = new StubHttpServletResponse();
        servlet.doGet(getReqOwner, getRespOwner);
        assertEquals(HttpServletResponse.SC_OK, getRespOwner.getStatus());
        String outputOwner = getRespOwner.getOutput();
        assertTrue(outputOwner.contains("privateId"));

        String otherEmail = "other@example.com";
        HttpSession otherMockSession = mock(HttpSession.class);
        when(otherMockSession.getAttribute("email")).thenReturn(otherEmail);
        StubHttpServletRequest getReqOther = new StubHttpServletRequest("", "GET", null, null, otherMockSession);
        StubHttpServletResponse getRespOther = new StubHttpServletResponse();
        servlet.doGet(getReqOther, getRespOther);
        assertEquals(HttpServletResponse.SC_OK, getRespOther.getStatus());
        String outputOther = getRespOther.getOutput();
        assertFalse(outputOther.contains("privateId"));
    }

    @Test
    public void testPermissionRead() throws Exception {
        Note note = createValidNote("readId");
        note.setPermission(Permission.READ);
        String json = NoteFactory.toJson(note);

        String ownerEmail = note.getOwnerEmail();
        HttpSession mockSession = mock(HttpSession.class);
        when(mockSession.getAttribute("email")).thenReturn(ownerEmail);

        StubHttpServletRequest postReq = new StubHttpServletRequest(json, "POST", null, null, mockSession);
        StubHttpServletResponse postResp = new StubHttpServletResponse();
        servlet.doPost(postReq, postResp);

        assertEquals(HttpServletResponse.SC_OK, postResp.getStatus());

        String otherEmail = "other@example.com";
        HttpSession otherMockSession = mock(HttpSession.class);
        when(otherMockSession.getAttribute("email")).thenReturn(otherEmail);
        StubHttpServletRequest getReq = new StubHttpServletRequest("", "GET", null, null, otherMockSession);
        StubHttpServletResponse getResp = new StubHttpServletResponse();
        servlet.doGet(getReq, getResp);
        assertEquals(HttpServletResponse.SC_OK, getResp.getStatus());
        String output = getResp.getOutput();
        assertTrue(output.contains("readId"));

        String putJson = "{\"title\":\"New\",\"content\":\"New\",\"updatedAt\":\"2025-05-22T10:06:02Z\",\"tags\":[\"testTag\"]}";
        StubHttpServletRequest putReq = new StubHttpServletRequest(putJson, "PUT", note.getId(), null, otherMockSession);
        StubHttpServletResponse putResp = new StubHttpServletResponse();
        servlet.doPut(putReq, putResp);
        assertEquals(HttpServletResponse.SC_FORBIDDEN, putResp.getStatus());

        StubHttpServletRequest deleteReq = new StubHttpServletRequest("", "DELETE", note.getId(), null, otherMockSession);
        StubHttpServletResponse deleteResp = new StubHttpServletResponse();
        servlet.doDelete(deleteReq, deleteResp);
        assertEquals(HttpServletResponse.SC_FORBIDDEN, deleteResp.getStatus());
    }

    @Test
    public void testPermissionWrite() throws Exception {
        Note note = createValidNote("writeId");
        note.setPermission(Permission.WRITE);
        String json = NoteFactory.toJson(note);

        String ownerEmail = note.getOwnerEmail();
        HttpSession mockSession = mock(HttpSession.class);
        when(mockSession.getAttribute("email")).thenReturn(ownerEmail);

        StubHttpServletRequest postReq = new StubHttpServletRequest(json, "POST", null, null, mockSession);
        StubHttpServletResponse postResp = new StubHttpServletResponse();
        servlet.doPost(postReq, postResp);

        assertEquals(HttpServletResponse.SC_OK, postResp.getStatus());

        String otherEmail = "other@example.com";
        HttpSession otherMockSession = mock(HttpSession.class);
        when(otherMockSession.getAttribute("email")).thenReturn(otherEmail);
        StubHttpServletRequest getReq = new StubHttpServletRequest("", "GET", null, null, otherMockSession);
        StubHttpServletResponse getResp = new StubHttpServletResponse();
        servlet.doGet(getReq, getResp);
        assertEquals(HttpServletResponse.SC_OK, getResp.getStatus());
        String output = getResp.getOutput();
        assertTrue(output.contains("writeId"));

        String putJson = "{\"title\":\"UpdatedTitle\",\"content\":\"UpdatedContent\",\"updatedAt\":\"2025-05-22T10:06:02Z\",\"tags\":[\"testTag\"]}";
        StubHttpServletRequest putReq = new StubHttpServletRequest(putJson, "PUT", note.getId(), null, otherMockSession);
        StubHttpServletResponse putResp = new StubHttpServletResponse();
        servlet.doPut(putReq, putResp);
        assertEquals(HttpServletResponse.SC_OK, putResp.getStatus());

        StubHttpServletRequest deleteReq = new StubHttpServletRequest("", "DELETE", note.getId(), null, otherMockSession);
        StubHttpServletResponse deleteResp = new StubHttpServletResponse();
        servlet.doDelete(deleteReq, deleteResp);
        assertEquals(HttpServletResponse.SC_OK, deleteResp.getStatus());
    }

    @Test
    public void testHiddenUser() throws Exception {
        Note note = createValidNote("alreadyHiddenId");
        note.setPermission(Permission.READ);
        String ownerEmail = note.getOwnerEmail();
        String hiddenUser = "hiddenUser@example.com";
        note.hideForUser(hiddenUser);

        NoteDB noteDB = NoteDB.getInstance(tempDbFileNote);
        HTreeMap<String, Note> noteMap = noteDB.getMap();
        noteMap.put(note.getId(), note);
        noteDB.commit();

        HttpSession ownerMockSession = mock(HttpSession.class);
        when(ownerMockSession.getAttribute("email")).thenReturn(ownerEmail);

        StubHttpServletRequest getReqOwner = new StubHttpServletRequest("", "GET", null, null, ownerMockSession);
        StubHttpServletResponse getRespOwner = new StubHttpServletResponse();
        servlet.doGet(getReqOwner, getRespOwner);
        assertEquals(HttpServletResponse.SC_OK, getRespOwner.getStatus());
        String outputOwner = getRespOwner.getOutput();
        assertTrue(outputOwner.contains("alreadyHiddenId"));

        HttpSession hiddenMockSession = mock(HttpSession.class);
        when(hiddenMockSession.getAttribute("email")).thenReturn(hiddenUser);
        StubHttpServletRequest getReqHidden = new StubHttpServletRequest("", "GET", null, null, hiddenMockSession);
        StubHttpServletResponse getRespHidden = new StubHttpServletResponse();
        servlet.doGet(getReqHidden, getRespHidden);
        assertEquals(HttpServletResponse.SC_OK, getRespHidden.getStatus());
        String outputHidden = getRespHidden.getOutput();
        assertFalse(outputHidden.contains("alreadyHiddenId"));
    }

    @Test
    public void testOwnerChangePermission() throws Exception {
        Note note = createValidNote("permId");
        note.setPermission(Permission.PRIVATE);
        String json = NoteFactory.toJson(note);

        String ownerEmail = note.getOwnerEmail();
        HttpSession mockSession = mock(HttpSession.class);
        when(mockSession.getAttribute("email")).thenReturn(ownerEmail);

        StubHttpServletRequest postReq = new StubHttpServletRequest(json, "POST", null, null, mockSession);
        StubHttpServletResponse postResp = new StubHttpServletResponse();
        servlet.doPost(postReq, postResp);
        assertEquals(HttpServletResponse.SC_OK, postResp.getStatus());

        String putJson = "{\"permission\":\"READ\",\"title\":\""
                + note.getCurrentVersion().getTitle()
                + "\",\"content\":\""
                + note.getCurrentVersion().getContent()
                + "\",\"updatedAt\":\"2025-06-05T10:00:00Z\"}";

        StubHttpServletRequest putReq = new StubHttpServletRequest(putJson, "PUT", note.getId(), null, mockSession);
        StubHttpServletResponse putResp = new StubHttpServletResponse();
        servlet.doPut(putReq, putResp);

        assertEquals(HttpServletResponse.SC_OK, putResp.getStatus());
    }

    @Test
    public void testNonOwnerChangePermission() throws Exception {
        Note note = createValidNote("permId2");
        note.setPermission(Permission.READ);
        String json = NoteFactory.toJson(note);

        String ownerEmail = note.getOwnerEmail();
        HttpSession mockSession = mock(HttpSession.class);
        when(mockSession.getAttribute("email")).thenReturn(ownerEmail);

        StubHttpServletRequest postReq = new StubHttpServletRequest(json, "POST", null, null, mockSession);
        StubHttpServletResponse postResp = new StubHttpServletResponse();
        servlet.doPost(postReq, postResp);
        assertEquals(HttpServletResponse.SC_OK, postResp.getStatus());

        String nonOwner = "nonOwner@example.com";
        HttpSession nonOwnerMockSession = mock(HttpSession.class);
        when(nonOwnerMockSession.getAttribute("email")).thenReturn(nonOwner);
        String putJson = "{\"permission\":\"PRIVATE\",\"title\":\""
                + note.getCurrentVersion().getTitle()
                + "\",\"content\":\""
                + note.getCurrentVersion().getContent()
                + "\",\"updatedAt\":\"2025-06-05T10:00:00Z\"}";

        StubHttpServletRequest putReq = new StubHttpServletRequest(putJson, "PUT", note.getId(), null, nonOwnerMockSession);
        StubHttpServletResponse putResp = new StubHttpServletResponse();
        servlet.doPut(putReq, putResp);
        assertEquals(HttpServletResponse.SC_FORBIDDEN, putResp.getStatus());
    }
}