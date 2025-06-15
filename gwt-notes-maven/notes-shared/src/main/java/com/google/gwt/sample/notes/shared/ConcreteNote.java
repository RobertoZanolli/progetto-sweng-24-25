package com.google.gwt.sample.notes.shared;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;

/**
 * Implementazione concreta dell'interfaccia Note.
 * Questa classe gestisce tutte le operazioni relative a una nota, inclusi i permessi,
 * le versioni e la visibilità per gli utenti.
 */
public class ConcreteNote implements Note {
    private String id;
    private Date createdAt;
    private String ownerEmail;
    private Permission permission;
    private String[] tags;
    private List<Version> versions;
    private Set<String> hiddenUsers;

    public ConcreteNote() {
        this.versions = new ArrayList<>();
        this.hiddenUsers = new HashSet<>();
    }

    public ConcreteNote(String id) {
        this.id = id;
    }

    // Getters e Setters (obbligatori per Gson)
    @Override
    public String getId() { 
        return this.id; 
    }
    @Override
    public void setId(String id) { 
        this.id = id; 
    }

    @Override
    public String getOwnerEmail() { 
        return this.ownerEmail; 
    }
    @Override
    public void setOwnerEmail(String ownerEmail) { 
        this.ownerEmail = ownerEmail; 
    }
    @Override
    public boolean isOwner(String userEmail) {
        return this.ownerEmail.equals(userEmail);
    }

    @Override
    public Permission getPermission() { 
        return this.permission; 
    }
    @Override
    public void setPermission(Permission permission) { 
        this.permission = permission; 
    }

    @Override
    public Date getCreatedAt() { 
        return this.createdAt; 
    }
    @Override
    public void setCreatedAt(Date createdDate) { 
        this.createdAt = createdDate; 
    }

    @Override
    public String[] getTags() { 
        return this.tags; 
    }
    @Override
    public void setTags(String[] tags) { 
        this.tags = tags; 
    }

    @Override
    public List<Version> getAllVersions() { 
        return this.versions; 
    }
    @Override
    public Version getCurrentVersion() { 
        return this.versions.get(this.versions.size()-1); 
    }
    @Override
    public void newVersion(Version version) { 
        this.versions.add(version); 
    }
    @Override
    public int currentVersionNumber() {
        return this.versions.size();
    }

    @Override
    public Set<String> getHiddenUsers() { 
        return this.hiddenUsers; 
    }
    @Override
    public boolean isHiddenForUser(String userEmail) { 
        return this.hiddenUsers.contains(userEmail); 
    }
    @Override
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
