package com.google.gwt.sample.notes.shared;

import java.io.Serializable;

public interface User extends Serializable {
    String getEmail();
    String getPassword();
    void setEmail(String email);
    void setPassword(String password);
}
