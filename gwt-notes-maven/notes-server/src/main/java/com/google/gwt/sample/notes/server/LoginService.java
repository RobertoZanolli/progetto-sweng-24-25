package com.google.gwt.sample.notes.server;

import com.google.gwt.sample.notes.shared.User;
import com.password4j.Password;
import org.mapdb.HTreeMap;
import java.io.File;

/**
 * Servizio per l'autenticazione degli utenti.
 * Verifica le credenziali e gestisce l'accesso al sistema.
 */
public class LoginService {
    private final UserDB userDB;

    public LoginService(File dbFile) {
        this.userDB = UserDB.getInstance(dbFile);
    }

    /**
     * Autentica un utente. Lancia ServiceException se le credenziali non sono valide.
     */
    public void authenticate(User user) throws ServiceException {
        HTreeMap<String, String> users = userDB.getMap();
        if (users == null) {
            throw new ServiceException("Database utenti non inizializzato", 500);
        }
        if (user == null || user.getEmail() == null || user.getPassword() == null
                || user.getEmail().isEmpty() || user.getPassword().isEmpty()) {
            throw new ServiceException("Email e password richieste", 400);
        }
        String hash = users.get(user.getEmail());
        if (hash == null || !Password.check(user.getPassword(), hash).withBcrypt()) {
            throw new ServiceException("Credenziali non valide", 401);
        }
    }

    public void close() {
        userDB.close();
    }
}
