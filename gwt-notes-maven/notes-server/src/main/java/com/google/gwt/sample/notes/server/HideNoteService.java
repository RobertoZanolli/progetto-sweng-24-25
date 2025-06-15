package com.google.gwt.sample.notes.server;

import com.google.gwt.sample.notes.shared.Note;
import org.mapdb.HTreeMap;
import java.io.File;

public class HideNoteService {
    private final NoteDB noteDB;

    public HideNoteService(File dbFile) {
        this.noteDB = NoteDB.getInstance(dbFile);
    }

    /**
     * Hides or unhides a note for a specific user.
     */
    public void hideNote(String noteId, String userEmail, boolean hide) throws ServiceException {
        HTreeMap<String, Note> noteMap = noteDB.getMap();
        if (noteMap == null) {
            throw new ServiceException("Notes database not initialized", 500);
        }
        if (userEmail == null || userEmail.isEmpty()) {
            throw new ServiceException("User not authenticated", 401);
        }
        if (noteId == null || noteId.isEmpty() || !noteMap.containsKey(noteId)) {
            throw new ServiceException("Note not found", 404);
        }
        Note note = noteMap.get(noteId);
        if (userEmail.equals(note.getOwnerEmail())) {
            throw new ServiceException("Owner cannot hide note", 400);
        }
        if (hide) {
            note.hideForUser(userEmail);
        }
        noteMap.put(noteId, note);
        noteDB.commit();
    }

    public void close() {
        noteDB.close();
    }
}
