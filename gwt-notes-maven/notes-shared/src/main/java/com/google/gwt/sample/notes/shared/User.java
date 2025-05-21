package com.google.gwt.sample.notes.shared;
/*Classe User:
 * - email
 * - passwordHash
 * Non vogliamo che gli utenti vedano la password, quindi non vogliamo 
 * che venga salvata in chiaro nel database. -> teniamo solo hash
 */
public class User {
    private String email;
    private String passwordHash;

    public User() {}

    public User(String email, String passwordHash) {
        this.email = email;
        this.passwordHash = passwordHash;
    }

    public String getEmail() {
        return email;
    }
    public String getPasswordHash() {
        return passwordHash;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
}