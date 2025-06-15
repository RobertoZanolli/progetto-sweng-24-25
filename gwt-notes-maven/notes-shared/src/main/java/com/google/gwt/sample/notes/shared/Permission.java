package com.google.gwt.sample.notes.shared;

/**
 * Enum che definisce i livelli di permesso per le note.
 * Ogni livello di permesso determina chi può visualizzare e modificare una nota.
 */
public enum Permission {
    /**
     * Permesso privato: solo il proprietario può visualizzare e modificare la nota
     */
    PRIVATE {
        @Override
        public boolean canView(String userEmail, Note note) {
            return note.isOwner(userEmail);
        }
        @Override
        public boolean canEdit(String userEmail, Note note) {
            return note.isOwner(userEmail);
        }
    },
    
    /**
     * Permesso di lettura: il proprietario può modificare, gli altri possono solo visualizzare
     */
    READ {
        @Override
        public boolean canView(String userEmail, Note note) {
            return note.isOwner(userEmail) || !note.isHiddenForUser(userEmail);
        }
        @Override
        public boolean canEdit(String userEmail, Note note) {
            return note.isOwner(userEmail);
        }
    },
    
    /**
     * Permesso di scrittura: il proprietario e gli altri utenti possono visualizzare e modificare
     */
    WRITE {
        @Override
        public boolean canView(String userEmail, Note note) {
            return note.isOwner(userEmail) || !note.isHiddenForUser(userEmail);
        }
        @Override
        public boolean canEdit(String userEmail, Note note) {
            return note.isOwner(userEmail) || !note.isHiddenForUser(userEmail);
        }
    };

    /**
     * Verifica se un utente può visualizzare una nota
     */
    public abstract boolean canView(String userEmail, Note note);
    
    /**
     * Verifica se un utente può modificare una nota
     */
    public abstract boolean canEdit(String userEmail, Note note);
}