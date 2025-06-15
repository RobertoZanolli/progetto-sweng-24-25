package com.google.gwt.sample.notes.shared;

import java.util.Date;
import java.io.Serializable;

/**
 * Implementazione concreta dell'interfaccia Version.
 * Questa classe rappresenta una singola versione di una nota con il suo titolo,
 * contenuto e data di aggiornamento.
 */
public class ConcreteVersion implements Serializable, Version {
    private String title;
    private String content;
    private Date updatedAt;

    public ConcreteVersion() {}

    @Override
    public String getTitle() { return title; }
    @Override
    public void setTitle(String title) { this.title = title; }

    @Override
    public String getContent() { return content; }
    @Override
    public void setContent(String content) { this.content = content; }

    @Override
    public Date getUpdatedAt() { return updatedAt; }
    @Override
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "Version{" +
                "title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
