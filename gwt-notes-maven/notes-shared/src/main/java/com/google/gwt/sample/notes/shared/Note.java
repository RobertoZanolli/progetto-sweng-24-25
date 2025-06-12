package com.google.gwt.sample.notes.shared;

import java.util.Date;
import java.util.List;
import java.util.Set;

public interface Note {
    String getId();
    void setId(String id);

    String getOwnerEmail();
    void setOwnerEmail(String ownerEmail);
    boolean isOwner(String userEmail);

    Permission getPermission();
    void setPermission(Permission permission);

    Date getCreatedAt();
    void setCreatedAt(Date createdDate);

    String[] getTags();
    void setTags(String[] tags);

    List<Version> getAllVersions();
    Version getCurrentVersion();
    void newVersion(Version version);
    int currentVersionNumber();

    Set<String> getHiddenUsers();
    boolean isHiddenForUser(String userEmail);
    boolean hideForUser(String userEmail);
}
