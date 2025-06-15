package com.google.gwt.sample.notes.shared;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.io.Serializable;

/**
 * Interfaccia che rappresenta una nota nel sistema.
 * Una nota è un documento che può essere condiviso tra utenti con diversi livelli di permesso.
 */
public interface Note extends Serializable {
    /**
     * Ottiene l'identificatore univoco della nota
     */
    String getId();
    void setId(String id);

    String getOwnerEmail();
    void setOwnerEmail(String ownerEmail);
    
    /**
     * Verifica se l'utente specificato è il proprietario della nota
     */
    boolean isOwner(String userEmail);

    /**
     * Ottiene il livello di permesso della nota
     */
    Permission getPermission();
    void setPermission(Permission permission);

    Date getCreatedAt();
    void setCreatedAt(Date createdDate);

    /**
     * Ottiene i tag associati alla nota
     */
    String[] getTags();
    void setTags(String[] tags);

    /**
     * Ottiene tutte le versioni della nota
     */
    List<Version> getAllVersions();
    
    /**
     * Ottiene l'ultima versione della nota
     */
    Version getCurrentVersion();
    
    /**
     * Aggiunge una nuova versione alla nota
     */
    void newVersion(Version version);
    
    /**
     * Ottiene il numero dell'ultima versione
     */
    int currentVersionNumber();

    /**
     * Ottiene l'insieme degli utenti per cui la nota è nascosta
     */
    Set<String> getHiddenUsers();
    
    /**
     * Verifica se la nota è nascosta per un utente specifico
     */
    boolean isHiddenForUser(String userEmail);
    
    /**
     * Nasconde la nota per un utente specifico
     * @return true se l'operazione è riuscita, false se l'utente è il proprietario
     */
    boolean hideForUser(String userEmail);
}
