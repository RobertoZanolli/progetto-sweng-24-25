package com.google.gwt.sample.notes.shared;

import java.io.Serializable;

/*Classe User:
 * - email
 * - password 
 *  L'utente ha mail e password,  
 */
public class User implements Serializable {
    private String email;
    private String password ;

    public User() {}

    public User(String email, String password ) {
        this.email = email;
        this.password  = password ;
    }

    public String getEmail() {
        return email;
    }
    public String getPassword () {
        return password ;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword (String password ) {
        this.password  = password ;
    }
}