package com.google.gwt.sample.notes.shared;

public class ConcreteTag implements Tag {
    private String name;

    public ConcreteTag() {}

    public ConcreteTag(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
    @Override
    public void setName(String name) {
        this.name = name;
    }
}
