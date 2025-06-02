package com.google.gwt.sample.notes.shared;

import java.util.Date;
import java.util.ArrayList;
import java.io.Serializable;

public class Note implements Serializable {
    private String id;
    private Date createdAt;
    private String ownerEmail;
    private Permission permissions;
    private String[] tags;
    private ArrayList<Version> versions;

    public Note() {
        this.versions = new ArrayList<>();
    }

    public Note(String id) {
        this.id = id;
    }

    // Getters e Setters (obbligatori per Gson + POJO style)
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getOwnerEmail() { return ownerEmail; }
    public void setOwnerEmail(String ownerEmail) { this.ownerEmail = ownerEmail; }

    public Permission getPermissions() { return permissions; }
    public void setPermissions(Permission permissions) { this.permissions = permissions; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdDate) { this.createdAt = createdDate; }

    public String[] getTags() { return tags; }
    public void setTags(String[] tags) { this.tags = tags; }

    public ArrayList<Version> getAllVersions() { return versions; }
    public Version getCurrentVersion() { return versions.get(versions.size()-1); }
    public void addVersion(Version version) { this.versions.add(version); }

    @Override
    public String toString() {
        return "Note{" +
                "id='" + id + '\'' +
                ", createdAt=" + createdAt +
                ", ownerEmail=" + ownerEmail +
                ", permissions=" + permissions +
                ", versionNumber=" + versions.size() +
                '}';
    }
}
