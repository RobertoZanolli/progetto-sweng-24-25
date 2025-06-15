package com.google.gwt.sample.notes.server;

import com.google.gwt.sample.notes.shared.Tag;
import org.mapdb.HTreeMap;
import java.io.File;
import java.util.Set;

/**
 * Servizio per la gestione dei tag.
 * Gestisce la creazione e il recupero dei tag.
 */
public class TagService {
    private final TagDB tagDB;

    public TagService(File dbFile) {
        this.tagDB = TagDB.getInstance(dbFile);
    }

    /**
     * Crea un nuovo tag.
     * @param tag 
     * @throws ServiceException 
     */
    public void createTag(Tag tag) throws ServiceException {
        if (tag == null || tag.getName() == null || tag.getName().isEmpty()) {
            throw new ServiceException("Nome del tag richiesto", 400);
        }
        HTreeMap<String, Tag> tagMap = tagDB.getMap();
        if (tagMap == null) {
            throw new ServiceException("Database dei tag non inizializzato", 500);
        }
        if (tagMap.containsKey(tag.getName())) {
            throw new ServiceException("Tag già esistente", 409);
        }
        tagMap.put(tag.getName(), tag);
        tagDB.commit();
    }

    /**
     * Recupera tutti i tag esistenti.
     * @return Set di nomi dei tag
     * @throws ServiceException 
     */
    public Set<String> getAllTags() throws ServiceException {
        HTreeMap<String, Tag> tagMap = tagDB.getMap();
        if (tagMap == null) {
            throw new ServiceException("Database dei tag non inizializzato", 500);
        }
        return tagMap.keySet();
    }

    public void close() {
        tagDB.close();
    }
}
