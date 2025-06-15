package com.google.gwt.sample.notes.server;

import com.google.gwt.sample.notes.shared.User;
import com.password4j.Password;
import org.mapdb.HTreeMap;
import java.io.File;

/**
 * Servizio per la registrazione degli utenti.
 * Gestisce la validazione e il salvataggio dei nuovi account.
 */
public class RegisterService {
    private final UserDB userDB;

    public RegisterService(File dbFile) {
        this.userDB = UserDB.getInstance(dbFile);
    }

    public void register(User user) throws ServiceException {
        HTreeMap<String, String> users = userDB.getMap();
        if (users == null) {
            throw new ServiceException("Database utenti non inizializzato", 500);
        }
        if (user == null || user.getEmail() == null || user.getEmail().isEmpty()) {
            throw new ServiceException("Email richiesta", 400);
        }
        if (!user.getEmail().contains("@")) {
            throw new ServiceException("Email non valida", 400);
        }
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new ServiceException("Password richiesta", 400);
        }
        if (users.containsKey(user.getEmail())) {
            throw new ServiceException("Utente già esistente", 409);
        }
        String hash = Password.hash(user.getPassword()).withBcrypt().getResult();
        users.put(user.getEmail(), hash);
        userDB.commit();
    }

    public void close() {
        userDB.close();
    }
}
