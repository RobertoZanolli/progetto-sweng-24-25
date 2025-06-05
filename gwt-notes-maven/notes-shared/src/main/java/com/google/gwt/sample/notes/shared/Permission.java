package com.google.gwt.sample.notes.shared;

public enum Permission {
    PRIVATE {
        @Override
        public boolean canView(String userEmail, Note note) {
            return note.getOwnerEmail().equals(userEmail);
        }
        @Override
        public boolean canEdit(String userEmail, Note note) {
            return note.getOwnerEmail().equals(userEmail);
        }
    },
    READ {
        @Override
        public boolean canView(String userEmail, Note note) {
            return true;
        }
        @Override
        public boolean canEdit(String userEmail, Note note) {
            return note.getOwnerEmail().equals(userEmail);
        }
    },
    WRITE {
        @Override
        public boolean canView(String userEmail, Note note) {
            return true;
        }
        @Override
        public boolean canEdit(String userEmail, Note note) {
            return true;
        }
    };

    public abstract boolean canView(String userEmail, Note note);
    public abstract boolean canEdit(String userEmail, Note note);
}