package com.google.gwt.sample.notes.shared;

public class ConcreteUser implements User {
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