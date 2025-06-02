package com.google.gwt.sample.notes.shared;

import java.util.Date;

public class Version {
    private String title;
    private String content;
    private Date editedAt;

    public Version() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Date getEditedAt() { return editedAt; }
    public void setEditedAt(Date editedAt) { this.editedAt = editedAt; }

    @Override
    public String toString() {
        return "Version{" +
                "title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", editedAt=" + editedAt +
                '}';
    }
}
