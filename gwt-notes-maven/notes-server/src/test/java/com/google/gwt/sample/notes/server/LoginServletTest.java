package com.google.gwt.sample.notes.server;

import com.google.gson.Gson;
import com.google.gwt.sample.notes.shared.User;
import com.password4j.Password;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mapdb.HTreeMap;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSessionContext;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;

import java.io.*;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.junit.Assert.*;

@SuppressWarnings("deprecation")
public class LoginServletTest {
    private LoginServlet servlet;
    private Gson gson = new Gson();
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

    /*
    private void setPrivateField(Object target, String fieldName, Object value) {
        try {
            Field field = LoginServlet.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field " + fieldName, e);
        }
    }
    */

    @Test
    public void testSuccessfulLogin() throws Exception {
        User user = new User();
        user.setEmail("testuser@test.it");
        user.setPassword("password123");
        String json = gson.toJson(user);

        StubHttpServletRequest req = new StubHttpServletRequest(json);
        StubHttpServletResponse resp = new StubHttpServletResponse();

        servlet.doPost(req, resp);
        assertEquals(HttpServletResponse.SC_OK, resp.getStatus());
        assertTrue(resp.getOutput().contains("Login successful"));
        assertNotNull(req.getSession(false));
        assertEquals("testuser@test.it", req.getSession().getAttribute("user"));
    }

    @Test
    public void testInvalidCredentials() throws Exception {
        User user = new User();
        user.setEmail("testuser@test.it");
        user.setPassword("wrongpassword");
        String json = gson.toJson(user);

        StubHttpServletRequest req = new StubHttpServletRequest(json);
        StubHttpServletResponse resp = new StubHttpServletResponse();

        servlet.doPost(req, resp);
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, resp.getStatus());
        assertTrue(resp.getOutput().contains("Invalid credentials"));
    }

    @Test
    public void testNonExistentUser() throws Exception {
        User user = new User();
        user.setEmail("nonexistent@test.it");
        user.setPassword("password123");
        String json = gson.toJson(user);

        StubHttpServletRequest req = new StubHttpServletRequest(json);
        StubHttpServletResponse resp = new StubHttpServletResponse();

        servlet.doPost(req, resp);
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, resp.getStatus());
        assertTrue(resp.getOutput().contains("Invalid credentials"));
    }
    
    // Inner classes for request/response stubs
    private static class StubHttpServletRequest implements javax.servlet.http.HttpServletRequest {
        private final BufferedReader reader;
        private StubHttpSession session;
        
        public StubHttpServletRequest(String body) {
            this.reader = new BufferedReader(new StringReader(body));
        }
        
        @Override
        public BufferedReader getReader() {
            return reader;
        }
        
        @Override
        public javax.servlet.http.HttpSession getSession() {
            if (session == null) {
                session = new StubHttpSession();
            }
            return session;
        }
        
        @Override
        public javax.servlet.http.HttpSession getSession(boolean create) {
            if (create && session == null) {
                session = new StubHttpSession();
            }
            return session;
        }
        
        // Minimal implementation of other required methods
        @Override public String getMethod() { return "POST"; }
        @Override public String getAuthType() { return null; }
        @Override public String getContextPath() { return null; }
        @Override public String getHeader(String name) { return null; }
        @Override public java.util.Enumeration<String> getHeaderNames() { return null; }
        @Override public String getPathInfo() { return null; }
        @Override public String getQueryString() { return null; }
        @Override public String getRemoteUser() { return null; }
        @Override public String getRequestURI() { return null; }
        @Override public StringBuffer getRequestURL() { return null; }
        @Override public String getRequestedSessionId() { return null; }
        @Override public String getServletPath() { return null; }
        @Override public boolean isRequestedSessionIdValid() { return false; }
        @Override public boolean isUserInRole(String role) { return false; }
        @Override public java.security.Principal getUserPrincipal() { return null; }
        @Override public Object getAttribute(String name) { return null; }
        @Override public String getParameter(String name) { return null; }

        @Override
        public Enumeration<String> getAttributeNames() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getAttributeNames'");
        }

        @Override
        public String getCharacterEncoding() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getCharacterEncoding'");
        }

        @Override
        public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'setCharacterEncoding'");
        }

        @Override
        public int getContentLength() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getContentLength'");
        }

        @Override
        public long getContentLengthLong() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getContentLengthLong'");
        }

        @Override
        public String getContentType() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getContentType'");
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getInputStream'");
        }

        @Override
        public Enumeration<String> getParameterNames() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getParameterNames'");
        }

        @Override
        public String[] getParameterValues(String name) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getParameterValues'");
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getParameterMap'");
        }

        @Override
        public String getProtocol() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getProtocol'");
        }

        @Override
        public String getScheme() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getScheme'");
        }

        @Override
        public String getServerName() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getServerName'");
        }

        @Override
        public int getServerPort() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getServerPort'");
        }

        @Override
        public String getRemoteAddr() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getRemoteAddr'");
        }

        @Override
        public String getRemoteHost() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getRemoteHost'");
        }

        @Override
        public void setAttribute(String name, Object o) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'setAttribute'");
        }

        @Override
        public void removeAttribute(String name) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'removeAttribute'");
        }

        @Override
        public Locale getLocale() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getLocale'");
        }

        @Override
        public Enumeration<Locale> getLocales() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getLocales'");
        }

        @Override
        public boolean isSecure() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'isSecure'");
        }

        @Override
        public RequestDispatcher getRequestDispatcher(String path) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getRequestDispatcher'");
        }

        @Override
        public String getRealPath(String path) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getRealPath'");
        }

        @Override
        public int getRemotePort() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getRemotePort'");
        }

        @Override
        public String getLocalName() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getLocalName'");
        }

        @Override
        public String getLocalAddr() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getLocalAddr'");
        }

        @Override
        public int getLocalPort() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getLocalPort'");
        }

        @Override
        public ServletContext getServletContext() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getServletContext'");
        }

        @Override
        public AsyncContext startAsync() throws IllegalStateException {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'startAsync'");
        }

        @Override
        public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse)
                throws IllegalStateException {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'startAsync'");
        }

        @Override
        public boolean isAsyncStarted() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'isAsyncStarted'");
        }

        @Override
        public boolean isAsyncSupported() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'isAsyncSupported'");
        }

        @Override
        public AsyncContext getAsyncContext() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getAsyncContext'");
        }

        @Override
        public DispatcherType getDispatcherType() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getDispatcherType'");
        }

        @Override
        public Cookie[] getCookies() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getCookies'");
        }

        @Override
        public long getDateHeader(String name) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getDateHeader'");
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getHeaders'");
        }

        @Override
        public int getIntHeader(String name) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getIntHeader'");
        }

        @Override
        public String getPathTranslated() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getPathTranslated'");
        }

        @Override
        public String changeSessionId() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'changeSessionId'");
        }

        @Override
        public boolean isRequestedSessionIdFromCookie() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'isRequestedSessionIdFromCookie'");
        }

        @Override
        public boolean isRequestedSessionIdFromURL() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'isRequestedSessionIdFromURL'");
        }

        @Override
        public boolean isRequestedSessionIdFromUrl() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'isRequestedSessionIdFromUrl'");
        }

        @Override
        public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'authenticate'");
        }

        @Override
        public void login(String username, String password) throws ServletException {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'login'");
        }

        @Override
        public void logout() throws ServletException {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'logout'");
        }

        @Override
        public Collection<Part> getParts() throws IOException, ServletException {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getParts'");
        }

        @Override
        public Part getPart(String name) throws IOException, ServletException {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getPart'");
        }

        @Override
        public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'upgrade'");
        }
        
        // Other methods would be implemented with UnsupportedOperationException
        // ... (omitted for brevity)
    }
    
    private static class StubHttpSession implements javax.servlet.http.HttpSession {
        private final Map<String, Object> attributes = new HashMap<>();
        
        @Override
        public Object getAttribute(String name) {
            return attributes.get(name);
        }
        
        @Override
        public void setAttribute(String name, Object value) {
            attributes.put(name, value);
        }
        
        @Override
        public void removeAttribute(String name) {
            attributes.remove(name);
        }
        
        // Minimal implementation of other required methods
        @Override public long getCreationTime() { return System.currentTimeMillis(); }
        @Override public String getId() { return "test-session-id"; }
        @Override public long getLastAccessedTime() { return System.currentTimeMillis(); }
        @Override public int getMaxInactiveInterval() { return 3600; }
        @Override public javax.servlet.ServletContext getServletContext() { return null; }
        @Override public void invalidate() { attributes.clear(); }
        @Override public boolean isNew() { return false; }
        @Override public void setMaxInactiveInterval(int interval) { }

        @Override
        public HttpSessionContext getSessionContext() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getSessionContext'");
        }

        @Override
        public Object getValue(String name) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getValue'");
        }

        @Override
        public Enumeration<String> getAttributeNames() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getAttributeNames'");
        }

        @Override
        public String[] getValueNames() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getValueNames'");
        }

        @Override
        public void putValue(String name, Object value) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'putValue'");
        }

        @Override
        public void removeValue(String name) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'removeValue'");
        }
        
    }
    
    private static class StubHttpServletResponse implements javax.servlet.http.HttpServletResponse {
        private final StringWriter sw = new StringWriter();
        private final PrintWriter pw = new PrintWriter(sw);
        private int status = 0;
        
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
        

        @Override public void addHeader(String name, String value) {}
        @Override public void setHeader(String name, String value) {}
        @Override public void sendError(int sc) throws IOException { this.status = sc; }
        @Override public void sendError(int sc, String msg) throws IOException { this.status = sc; }
        @Override public void sendRedirect(String location) throws IOException {}
        @Override public String getCharacterEncoding() { return "UTF-8"; }
        @Override public String getContentType() { return null; }
        @Override public void setContentType(String type) {}
        @Override public void setCharacterEncoding(String charset) {}
        @Override public void setContentLength(int len) {}
        @Override public void setContentLengthLong(long len) {}

        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getOutputStream'");
        }

        @Override
        public void setBufferSize(int size) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'setBufferSize'");
        }

        @Override
        public int getBufferSize() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getBufferSize'");
        }

        @Override
        public void flushBuffer() throws IOException {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'flushBuffer'");
        }

        @Override
        public void resetBuffer() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'resetBuffer'");
        }

        @Override
        public boolean isCommitted() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'isCommitted'");
        }

        @Override
        public void reset() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'reset'");
        }

        @Override
        public void setLocale(Locale loc) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'setLocale'");
        }

        @Override
        public Locale getLocale() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getLocale'");
        }

        @Override
        public void addCookie(Cookie cookie) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'addCookie'");
        }

        @Override
        public boolean containsHeader(String name) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'containsHeader'");
        }

        @Override
        public String encodeURL(String url) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'encodeURL'");
        }

        @Override
        public String encodeRedirectURL(String url) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'encodeRedirectURL'");
        }

        @Override
        public String encodeUrl(String url) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'encodeUrl'");
        }

        @Override
        public String encodeRedirectUrl(String url) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'encodeRedirectUrl'");
        }

        @Override
        public void setDateHeader(String name, long date) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'setDateHeader'");
        }

        @Override
        public void addDateHeader(String name, long date) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'addDateHeader'");
        }

        @Override
        public void setIntHeader(String name, int value) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'setIntHeader'");
        }

        @Override
        public void addIntHeader(String name, int value) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'addIntHeader'");
        }

        @Override
        public void setStatus(int sc, String sm) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'setStatus'");
        }

        @Override
        public String getHeader(String name) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getHeader'");
        }

        @Override
        public Collection<String> getHeaders(String name) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getHeaders'");
        }

        @Override
        public Collection<String> getHeaderNames() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getHeaderNames'");
        }
        
        // Other methods would be implemented with UnsupportedOperationException
        // ... (omitted for brevity)
    }
}