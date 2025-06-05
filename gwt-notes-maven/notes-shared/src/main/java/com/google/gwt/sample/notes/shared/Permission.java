package com.google.gwt.sample.notes.shared;

public enum Permission {
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

    public abstract boolean canView(String userEmail, Note note);
    public abstract boolean canEdit(String userEmail, Note note);
}