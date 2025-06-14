package com.google.gwt.sample.notes.shared;

import java.util.Date;

public interface Version {
    String getTitle();
    void setTitle(String title);

    String getContent();
    void setContent(String content);

    Date getUpdatedAt();
    void setUpdatedAt(Date updatedAt);
}
