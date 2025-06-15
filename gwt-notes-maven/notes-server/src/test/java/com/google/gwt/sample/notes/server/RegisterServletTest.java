package com.google.gwt.sample.notes.server;

import com.google.gwt.sample.notes.shared.ConcreteUser;
import com.google.gwt.sample.notes.shared.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mapdb.HTreeMap;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponseWrapper;

import java.io.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class RegisterServletTest {
    private RegisterServlet servlet;
    private File tempDbFile;

    @Before
    public void setUp() throws IOException {
        if (tempDbFile != null && tempDbFile.exists()) {
            if (!tempDbFile.delete()) {
                throw new IOException("Could not delete old temp DB file: " + tempDbFile.getAbsolutePath());
            }
        }
        tempDbFile = File.createTempFile("register-test", ".db");
        if (tempDbFile.exists() && !tempDbFile.delete()) {
            throw new IOException("Could not delete just-created temp DB file: " + tempDbFile.getAbsolutePath());
        }
        String dbPath = tempDbFile.getAbsolutePath();
        servlet = new RegisterServlet(dbPath);
        servlet.init();
        
        assertNotNull("UserDB should be initialized", UserDB.getInstance(tempDbFile));
        assertNotNull("UserDB map should be initialized", UserDB.getInstance(tempDbFile).getMap());
    }

    @After
    public void tearDown() {
        UserDB.resetInstance();
        if (tempDbFile != null && tempDbFile.exists()) {
            tempDbFile.delete();
        }
    }

    @Test
    public void testSuccessfulRegistration() throws Exception {
        User user = new ConcreteUser();
        user.setEmail("newuser@test.it");
        user.setPassword("password123");
        String json = UserFactory.toJson(user);

        StubHttpServletRequest req = new StubHttpServletRequest(json);
        StubHttpServletResponse resp = new StubHttpServletResponse();

        servlet.doPost(req, resp);
        assertEquals(HttpServletResponse.SC_OK, resp.getStatus());
        assertTrue(resp.getOutput().contains("User registered"));
        
        // Verify user was actually added to DB
        HTreeMap<String, String> users = UserDB.getInstance(tempDbFile).getMap();
        assertTrue(users.containsKey("newuser@test.it"));
    }

    @Test
    public void testDuplicateRegistration() throws Exception {
        // First registration
        User user = new ConcreteUser();
        user.setEmail("duplicate@test.it");
        user.setPassword("password123");
        String json = UserFactory.toJson(user);

        StubHttpServletRequest req1 = new StubHttpServletRequest(json);
        StubHttpServletResponse resp1 = new StubHttpServletResponse();
        servlet.doPost(req1, resp1);

        // Second registration with same email
        StubHttpServletRequest req2 = new StubHttpServletRequest(json);
        StubHttpServletResponse resp2 = new StubHttpServletResponse();
        servlet.doPost(req2, resp2);

        assertEquals(HttpServletResponse.SC_CONFLICT, resp2.getStatus());
        assertTrue(resp2.getOutput().contains("User already exists"));
    }

    @Test
    public void testInvalidEmail() throws Exception {
        User user = new ConcreteUser();
        user.setEmail("invalidemail");
        user.setPassword("password123");
        String json = UserFactory.toJson(user);

        StubHttpServletRequest req = new StubHttpServletRequest(json);
        StubHttpServletResponse resp = new StubHttpServletResponse();

        servlet.doPost(req, resp);
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, resp.getStatus());
        assertTrue(resp.getOutput().contains("invalid email"));
    }

    private static class StubHttpServletRequest extends HttpServletRequestWrapper {
        private final BufferedReader reader;
        
        public StubHttpServletRequest(String body) {
            super(mock(HttpServletRequest.class));
            this.reader = new BufferedReader(new StringReader(body));
        }
        
        @Override
        public BufferedReader getReader() {
            return reader;
        }
        
        @Override
        public String getMethod() {
            return "POST";
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

    @Test
    public void testRegisterNewUser() throws Exception {
        User user = new ConcreteUser();
        user.setEmail("testuser@test.it");
        user.setPassword("password123");
        String json = UserFactory.toJson(user);

        StubHttpServletRequest req = new StubHttpServletRequest(json);
        StubHttpServletResponse resp = new StubHttpServletResponse();

        servlet.doPost(req, resp);
        assertEquals(HttpServletResponse.SC_OK, resp.getStatus());
        assertTrue(resp.getOutput().contains("User registered"));
    }

    @Test
    public void testRegisterDuplicateUser() throws Exception {
        User user = new ConcreteUser();
        user.setEmail("dupeuser@test.it");
        user.setPassword("password123");
        String json = UserFactory.toJson(user);

        StubHttpServletRequest req1 = new StubHttpServletRequest(json);
        StubHttpServletResponse resp1 = new StubHttpServletResponse();
        servlet.doPost(req1, resp1);
        assertEquals(HttpServletResponse.SC_OK, resp1.getStatus());
        assertTrue(resp1.getOutput().contains("User registered"));

        StubHttpServletRequest req2 = new StubHttpServletRequest(json);
        StubHttpServletResponse resp2 = new StubHttpServletResponse();
        servlet.doPost(req2, resp2);
        assertEquals(HttpServletResponse.SC_CONFLICT, resp2.getStatus());
        assertTrue(resp2.getOutput().contains("User already exists"));
    }

    @Test
    public void testRegisterWithInvalidEmail() throws Exception {
        User user = new ConcreteUser();
        user.setEmail("invalidemail.com");
        user.setPassword("password123");
        String json = UserFactory.toJson(user);

        StubHttpServletRequest req = new StubHttpServletRequest(json);
        StubHttpServletResponse resp = new StubHttpServletResponse();

        servlet.doPost(req, resp);
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, resp.getStatus());
        assertTrue(resp.getOutput().contains("invalid email"));
    }

    @Test
    public void testRegisterWithNullEmail() throws Exception {
        User user = new ConcreteUser();
        user.setEmail(null);
        user.setPassword("password123");
        String json = UserFactory.toJson(user);

        StubHttpServletRequest req = new StubHttpServletRequest(json);
        StubHttpServletResponse resp = new StubHttpServletResponse();

        servlet.doPost(req, resp);
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, resp.getStatus());
        assertTrue(resp.getOutput().contains("email required"));
    }

    @Test
    public void testRegisterWithEmptyEmail() throws Exception {
        User user = new ConcreteUser();
        user.setEmail("");
        user.setPassword("password123");
        String json = UserFactory.toJson(user);

        StubHttpServletRequest req = new StubHttpServletRequest(json);
        StubHttpServletResponse resp = new StubHttpServletResponse();

        servlet.doPost(req, resp);
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, resp.getStatus());
        assertTrue(resp.getOutput().contains("email required"));
    }

    @Test
    public void testRegisterWithNullPassword() throws Exception {
        User user = new ConcreteUser();
        user.setEmail("test@domain.com");
        user.setPassword(null);
        String json = UserFactory.toJson(user);

        StubHttpServletRequest req = new StubHttpServletRequest(json);
        StubHttpServletResponse resp = new StubHttpServletResponse();

        servlet.doPost(req, resp);
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, resp.getStatus());
        assertTrue(resp.getOutput().contains("password required"));
    }

    @Test
    public void testRegisterWithEmptyPassword() throws Exception {
        User user = new ConcreteUser();
        user.setEmail("test@domain.com");
        user.setPassword("");
        String json = UserFactory.toJson(user);

        StubHttpServletRequest req = new StubHttpServletRequest(json);
        StubHttpServletResponse resp = new StubHttpServletResponse();

        servlet.doPost(req, resp);
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, resp.getStatus());
        assertTrue(resp.getOutput().contains("password required"));
    }
}