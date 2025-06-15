package com.google.gwt.sample.notes.server;

import com.google.gwt.sample.notes.shared.Note;
import org.mapdb.HTreeMap;
import java.io.File;

/**
 * Servizio per gestire la visibilità delle note per gli utenti.
 * Permette di nascondere/mostrare note per utenti specifici.
 */
public class HideNoteService {
    private final NoteDB noteDB;

    public HideNoteService(File dbFile) {
        this.noteDB = NoteDB.getInstance(dbFile);
    }

    /**
     * Nasconde o mostra una nota per un utente specifico.
     * @param noteId 
     * @param userEmail 
     * @param hide true per nascondere, false per mostrare
     * @throws ServiceException 
     */
    public void hideNote(String noteId, String userEmail, boolean hide) throws ServiceException {
        HTreeMap<String, Note> noteMap = noteDB.getMap();
        if (noteMap == null) {
            throw new ServiceException("Database note non inizializzato", 500);
        }
        if (userEmail == null || userEmail.isEmpty()) {
            throw new ServiceException("Utente non autenticato", 401);
        }
        if (noteId == null || noteId.isEmpty() || !noteMap.containsKey(noteId)) {
            throw new ServiceException("Nota non trovata", 404);
        }
        Note note = noteMap.get(noteId);
        if (userEmail.equals(note.getOwnerEmail())) {
            throw new ServiceException("Il proprietario non può nascondersi la nota", 400);
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
