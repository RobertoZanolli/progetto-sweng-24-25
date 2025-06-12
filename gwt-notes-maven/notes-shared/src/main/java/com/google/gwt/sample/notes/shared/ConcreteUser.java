package com.google.gwt.sample.notes.shared;

import java.io.Serializable;

/*Classe User:
 * - email
 * - password 
 *  L'utente ha mail e password,  
 */
public class ConcreteUser implements Serializable, User {
    private String email;
    private String password ;

    public ConcreteUser() {}

    public ConcreteUser(String email, String password ) {
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