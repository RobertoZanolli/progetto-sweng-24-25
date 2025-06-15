package com.google.gwt.sample.notes.shared;

import java.util.Date;

/**
 * Interfaccia che rappresenta una versione di una nota.
 * Ogni versione contiene un titolo, un contenuto e la data dell'ultimo aggiornamento.
 */
public interface Version {
    /**
     * Ottiene il titolo della versione
     */
    String getTitle();
    void setTitle(String title);

    /**
     * Ottiene il contenuto della versione
     */
    String getContent();
    void setContent(String content);

    /**
     * Ottiene la data dell'ultimo aggiornamento
     */
    Date getUpdatedAt();
    void setUpdatedAt(Date updatedAt);
}
