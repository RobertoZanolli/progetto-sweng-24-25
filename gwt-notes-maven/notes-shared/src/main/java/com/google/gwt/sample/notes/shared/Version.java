package com.google.gwt.sample.notes.shared;

import java.util.Date;
import java.io.Serializable;

public class Version implements Serializable {
    private String title;
    private String content;
    private Date updatedAt;

    public Version() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Date getUpdatedAt() { return updatedAt; }
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
