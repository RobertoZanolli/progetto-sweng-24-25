package com.google.gwt.sample.notes.shared;

import java.io.Serializable;

public class Tag implements Serializable {
    private String name;

    public Tag() {
        // Default constructor for serialization
    }

    public Tag(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
}
