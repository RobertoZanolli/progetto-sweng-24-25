package com.google.gwt.sample.notes.server;

import com.google.gwt.sample.notes.shared.Tag;
import org.mapdb.HTreeMap;
import java.io.File;
import java.util.Set;

public class TagService {
    private final TagDB tagDB;

    public TagService(File dbFile) {
        this.tagDB = TagDB.getInstance(dbFile);
    }

    public void createTag(Tag tag) throws ServiceException {
        if (tag == null || tag.getName() == null || tag.getName().isEmpty()) {
            throw new ServiceException("Name required", 400);
        }
        HTreeMap<String, Tag> tagMap = tagDB.getMap();
        if (tagMap == null) {
            throw new ServiceException("Tag database not initialized", 500);
        }
        if (tagMap.containsKey(tag.getName())) {
            throw new ServiceException("Tag already exists", 409);
        }
        tagMap.put(tag.getName(), tag);
        tagDB.commit();
    }

    public Set<String> getAllTags() throws ServiceException {
        HTreeMap<String, Tag> tagMap = tagDB.getMap();
        if (tagMap == null) {
            throw new ServiceException("Tag database not initialized", 500);
        }
        return tagMap.keySet();
    }

    public void close() {
        tagDB.close();
    }
}
