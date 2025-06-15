package com.google.gwt.sample.notes.shared;

/**
 * Implementazione concreta dell'interfaccia Tag.
 * Questa classe rappresenta un singolo tag con il suo nome.
 */
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
