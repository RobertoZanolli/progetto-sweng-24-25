package com.google.gwt.sample.notes.server;

import com.google.gwt.sample.notes.shared.User;
import com.password4j.Password;
import org.mapdb.HTreeMap;
import java.io.File;

public class LoginService {
    private final UserDB userDB;

    public LoginService(File dbFile) {
        this.userDB = UserDB.getInstance(dbFile);
    }

    /**
     * Authenticates a user. Throws ServiceException if credentials are invalid.
     */
    public void authenticate(User user) throws ServiceException {
        HTreeMap<String, String> users = userDB.getMap();
        if (users == null) {
            throw new ServiceException("User database not initialized", 500);
        }
        if (user == null || user.getEmail() == null || user.getPassword() == null
                || user.getEmail().isEmpty() || user.getPassword().isEmpty()) {
            throw new ServiceException("Email and password required", 400);
        }
        String hash = users.get(user.getEmail());
        if (hash == null || !Password.check(user.getPassword(), hash).withBcrypt()) {
            throw new ServiceException("Invalid credentials", 401);
        }
    }

    public void close() {
        userDB.close();
    }
}
