package com.google.gwt.sample.notes.shared;

import java.io.Serializable;

/**
 * Interfaccia che rappresenta un utente nel sistema.
 * Un utente è identificato dalla sua email e password.
 */
public interface User extends Serializable {

    String getEmail();
    
    String getPassword();

    void setEmail(String email);

    void setPassword(String password);
}
