package com.google.gwt.sample.notes.shared;

import java.io.Serializable;

/**
 * Interfaccia che rappresenta un tag nel sistema.
 */
public interface Tag extends Serializable {
    /**
     * Ottiene il nome del tag
     */
    String getName();
    void setName(String name);
}
