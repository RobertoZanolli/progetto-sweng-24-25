package com.google.gwt.sample.notes.server;

import com.google.gwt.sample.notes.shared.ConcreteUser;
import com.google.gwt.sample.notes.shared.User;
import com.password4j.Password;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mapdb.HTreeMap;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.HttpSession;

import java.io.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Test per LoginServlet.
 * Verifica l'autenticazione degli utenti e la gestione degli errori.
 */
public class LoginServletTest {
    private LoginServlet servlet;
    private File tempDbFile;
    
    @Before
    public void setUp() throws IOException {
        System.out.println("ENTRA");
        // UserDB.resetInstance();
        if (tempDbFile != null && tempDbFile.exists()) {
            if (!tempDbFile.delete()) {
                throw new IOException("Could not delete old temp DB file: " + tempDbFile.getAbsolutePath());
            }
        }
        tempDbFile = File.createTempFile("login-test", ".db");
        if (tempDbFile.exists() && !tempDbFile.delete()) {
            throw new IOException("Could not delete just-created temp DB file: " + tempDbFile.getAbsolutePath());
        }
        String dbPath = tempDbFile.getAbsolutePath();
        servlet = new LoginServlet(dbPath);
        servlet.init();
        // Use UserDB to add the test user
        HTreeMap<String, String> users = UserDB.getInstance(tempDbFile).getMap();

        assertNotNull("UserDB should be initialized", UserDB.getInstance(tempDbFile));
        assertNotNull("UserDB map should be initialized", users);
        
        String hashedPassword = Password.hash("password123").withBcrypt().getResult();
        users.put("testuser@test.it", hashedPassword);
        UserDB.getInstance(tempDbFile).commit();
    }

    @After
    public void tearDown() {
        UserDB.resetInstance();
        if (tempDbFile != null && tempDbFile.exists()) tempDbFile.delete();
    }

    @Test
    public void testSuccessfulLogin() throws Exception {
        User user = new ConcreteUser();
        user.setEmail("testuser@test.it");
        user.setPassword("password123");
        String json = UserFactory.toJson(user);

        StubHttpServletRequest req = new StubHttpServletRequest(json);
        StubHttpServletResponse resp = new StubHttpServletResponse();

        servlet.doPost(req, resp);
        assertEquals(HttpServletResponse.SC_OK, resp.getStatus());
        assertTrue(resp.getOutput().contains("Login effettuato con successo"));
    }

    @Test
    public void testInvalidCredentials() throws Exception {
        User user = new ConcreteUser();
        user.setEmail("testuser@test.it");
        user.setPassword("wrongpassword");
        String json = UserFactory.toJson(user);

        StubHttpServletRequest req = new StubHttpServletRequest(json);
        StubHttpServletResponse resp = new StubHttpServletResponse();

        servlet.doPost(req, resp);
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, resp.getStatus());
        assertTrue(resp.getOutput().contains("Credenziali non valide"));
    }

    @Test
    public void testNonExistentUser() throws Exception {
        User user = new ConcreteUser();
        user.setEmail("nonexistent@test.it");
        user.setPassword("password123");
        String json = UserFactory.toJson(user);

        StubHttpServletRequest req = new StubHttpServletRequest(json);
        StubHttpServletResponse resp = new StubHttpServletResponse();

        servlet.doPost(req, resp);
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, resp.getStatus());
        assertTrue(resp.getOutput().contains("Credenziali non valide"));
    }
    
    // Inner classes for request/response stubs
    private static class StubHttpServletRequest extends HttpServletRequestWrapper {
        private final BufferedReader reader;
        private final HttpSession mockSession;
        
        public StubHttpServletRequest(String body) {
            super(mock(HttpServletRequest.class));
            this.reader = new BufferedReader(new StringReader(body));
            this.mockSession = mock(HttpSession.class);
        }
        
        @Override
        public BufferedReader getReader() {
            return reader;
        }
        
        @Override
        public HttpSession getSession() {
            return mockSession;
        }
        
        @Override
        public HttpSession getSession(boolean create) {
            return mockSession;
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
}