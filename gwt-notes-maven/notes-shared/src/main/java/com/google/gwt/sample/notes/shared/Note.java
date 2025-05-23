package com.google.gwt.sample.notes.shared;

import java.util.Date;
import java.io.Serializable;
import java.util.Arrays;

public class Note implements Serializable{
    private String id;
    private String title;
    private String content;
    private Date createdDate;
    private Date lastModifiedDate;
    private String[] tags;
    private User owner;

    public Note() {
        NoteIdGenerator generator = new NoteIdGenerator(1);
        long id = generator.nextId();
        this.id = Long.toString(id); 
    }

    // Getters e Setters (obbligatori per Gson + POJO style)
    public String getId() { return id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Date getCreatedDate() { return createdDate; }
    public void setCreatedDate(Date createdDate) { this.createdDate = createdDate; }

    public Date getLastModifiedDate() { return lastModifiedDate; }
    public void setLastModifiedDate(Date lastModifiedDate) { this.lastModifiedDate = lastModifiedDate; }

    public String[] getTags() { return tags; }
    public void setTags(String[] tags) { this.tags = tags; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    @Override
    public String toString() {
        return "Note{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", createdDate=" + createdDate +
                ", lastModifiedDate=" + lastModifiedDate +
                ", tags=" + Arrays.toString(tags) +
                ", owner=" + owner +
                '}';
    }
}
