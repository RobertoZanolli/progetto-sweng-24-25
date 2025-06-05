package com.google.gwt.sample.notes.shared;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.io.Serializable;

public class Note implements Serializable {
    private String id;
    private Date createdAt;
    private String ownerEmail;
    private Permission permission;
    private String[] tags;
    private List<Version> versions;
    private Set<String> hiddenUsers;

    public Note() {
        this.versions = new ArrayList<>();
        this.hiddenUsers = new HashSet<>();
    }

    public Note(String id) {
        this.id = id;
    }

    // Getters e Setters (obbligatori per Gson + POJO style)
    public String getId() { 
        return this.id; 
    }
    public void setId(String id) { 
        this.id = id; 
    }

    public String getOwnerEmail() { 
        return this.ownerEmail; 
    }
    public void setOwnerEmail(String ownerEmail) { 
        this.ownerEmail = ownerEmail; 
    }
    public boolean isOwner(String userEmail) {
        return this.ownerEmail.equals(userEmail);
    }

    public Permission getPermission() { 
        return this.permission; 
    }
    public void setPermission(Permission permission) { 
        this.permission = permission; 
    }

    public Date getCreatedAt() { 
        return this.createdAt; 
    }
    public void setCreatedAt(Date createdDate) { 
        this.createdAt = createdDate; 
    }

    public String[] getTags() { 
        return this.tags; 
    }
    public void setTags(String[] tags) { 
        this.tags = tags; 
    }

    public List<Version> getAllVersions() { 
        return this.versions; 
    }
    public Version getCurrentVersion() { 
        return this.versions.get(this.versions.size()-1); 
    }
    public void newVersion(Version version) { 
        this.versions.add(version); 
    }
    public int currentVersionNumber() {
        return this.versions.size();
    }

    public Set<String> getHiddenUsers() { 
        return this.hiddenUsers; 
    }
    public boolean isHiddenForUser(String userEmail) { 
        return this.hiddenUsers.contains(userEmail); 
    }
    public boolean hideForUser(String userEmail) { 
        if(userEmail.equals(this.ownerEmail)) {
            return false;
        }
        return this.hiddenUsers.add(userEmail);
    }

    @Override
    public String toString() {
        return "Note{" +
                "id='" + this.id + '\'' +
                ", createdAt=" + this.createdAt +
                ", ownerEmail=" + this.ownerEmail +
                ", permission=" + this.permission +
                ", tags=" + this.tags +
                ", hiddenUsers=" + this.hiddenUsers +
                ", currentVersionNumber=" + this.currentVersionNumber() +
                ", versions=" + this.versions +
                '}';
    }
}
