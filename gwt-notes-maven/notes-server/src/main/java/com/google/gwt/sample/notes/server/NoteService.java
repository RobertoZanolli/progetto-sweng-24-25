package com.google.gwt.sample.notes.server;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gwt.sample.notes.shared.Note;
import com.google.gwt.sample.notes.shared.Permission;
import com.google.gwt.sample.notes.shared.Version;

import java.util.ArrayList;
import java.util.List;

/**
 * Servizio principale per la gestione delle note.
 * Gestisce operazioni CRUD e controlli di autorizzazione.
 */
public class NoteService {

    private final NoteDB noteDB;
    private final TagDB tagDB;


    public NoteService(NoteDB noteDB, TagDB tagDB) {
        this.noteDB = noteDB;
        this.tagDB = tagDB;
    }

    /**
     * Crea una nuova nota.
     * @param noteJson JSON della nota da creare
     * @param userEmail 
     * @throws ServiceException 
     */
    public void createNote(String noteJson, String userEmail) throws ServiceException {
        if (noteDB.getMap() == null || tagDB.getMap() == null) {
            throw new ServiceException("Database non inizializzato", 500);
        }

        Note note;
        try {
            note = NoteFactory.fromJson(noteJson);
        } catch (Exception e) {
            throw new ServiceException("Dati della nota non validi: " + e.getMessage(), 400);
        }

        if (note == null) {
            throw new ServiceException("Dati della nota non validi", 400);
        }

        if (userEmail == null || userEmail.isEmpty()) {
            throw new ServiceException("Utente non autenticato", 401);
        }

        if (note.getOwnerEmail() == null || note.getOwnerEmail().isEmpty()) {
            note.setOwnerEmail(userEmail);
        } else if (!note.getOwnerEmail().equals(userEmail)) {
            throw new ServiceException("L'email del proprietario non può essere diversa dall'email della sessione", 400);
        }

        if (note.getId() == null || note.getId().isEmpty()) {
            throw new ServiceException("ID della nota richiesto", 400);
        }

        if (noteDB.getMap().containsKey(note.getId())) {
            throw new ServiceException("Nota già esistente", 409);
        }

        if (note.getAllVersions() == null || note.getAllVersions().isEmpty()) {
            throw new ServiceException("È richiesta almeno una versione", 400);
        }

        if (note.getCurrentVersion() == null || note.getCurrentVersion().getTitle() == null
                || note.getCurrentVersion().getTitle().isEmpty()) {
            throw new ServiceException("Titolo richiesto", 400);
        }

        // Verifica l'esistenza dei tag
        if (note.getTags() != null) {
            for (String tag : note.getTags()) {
                if (tag == null || tag.isEmpty()) {
                    throw new ServiceException("Nome del tag richiesto", 400);
                }
                if (!tagDB.getMap().containsKey(tag)) {
                    throw new ServiceException("Il tag " + tag + " non esiste", 400);
                }
            }
        }

        noteDB.getMap().put(note.getId(), note);
        noteDB.commit();
    }

    /**
     * Recupera una nota specifica controllando i permessi dell'utente.
     * @param noteId 
     * @param userEmail 
     * @return La nota richiesta
     * @throws ServiceException 
     */
    public Note getNoteById(String noteId, String userEmail) throws ServiceException {
        if (userEmail == null || userEmail.isEmpty()) {
            throw new ServiceException("Utente non autenticato", 401);
        }

        if (noteId == null || noteId.isEmpty()) {
            throw new ServiceException("ID della nota richiesto", 400);
        }

        if (noteDB.getMap() == null) {
            throw new ServiceException("Database delle note non inizializzato", 500);
        }

        Note note = noteDB.getMap().get(noteId);
        if (note == null) {
            throw new ServiceException("Nota con ID " + noteId + " non trovata", 404);
        }

        if (!note.getPermission().canView(userEmail, note)) {
            throw new ServiceException("L'utente " + userEmail + " non ha il permesso di visualizzare la nota " + noteId, 403);
        }
        return note;
    }

    /**
     * Recupera tutte le note visibili per un utente.
     * @param userEmail 
     * @return Lista delle note visibili
     * @throws ServiceException
     */
    public List<Note> getAllVisibleNotes(String userEmail) throws ServiceException {
        if (userEmail == null || userEmail.isEmpty()) {
            throw new ServiceException("Utente non autenticato", 401);
        }

        if (noteDB.getMap() == null) {
            throw new ServiceException("Database delle note non inizializzato", 500);
        }

        List<Note> visibleNotes = new ArrayList<>();
        for (Note note : noteDB.getMap().values()) {
            if (note.getPermission().canView(userEmail, note)) {
                visibleNotes.add(note);
            }
        }
        return visibleNotes;
    }

    /**
     * Elimina una nota controllando i permessi dell'utente.
     * @param noteId 
     * @param userEmail 
     * @throws ServiceException 
     */
    public void deleteNote(String noteId, String userEmail) throws ServiceException {
        if (noteDB.getMap() == null) {
            throw new ServiceException("Database delle note non inizializzato", 500);
        }

        if (noteId == null || noteId.isEmpty()) {
            throw new ServiceException("ID della nota richiesto", 400);
        }

        if (!noteDB.getMap().containsKey(noteId)) {
            throw new ServiceException("Nota con ID " + noteId + " non trovata", 404);
        }

        Note noteToDelete = noteDB.getMap().get(noteId);
        if (!noteToDelete.getOwnerEmail().equals(userEmail) && !noteToDelete.getPermission().canEdit(userEmail, noteToDelete)) {
            throw new ServiceException("L'utente " + userEmail + " non ha il permesso di eliminare la nota " + noteId, 403);
        }

        noteDB.getMap().remove(noteId);
        noteDB.commit();
    }

    /**
     * Aggiorna una nota esistente aggiungendo una nuova versione.
     * @param noteId 
     * @param updateJson JSON con i dati di aggiornamento
     * @param userEmail 
     * @throws ServiceException
     */
    @SuppressWarnings("deprecation")
    public void updateNote(String noteId, String updateJson, String userEmail) throws ServiceException {
        if (noteDB.getMap() == null || tagDB.getMap() == null) {
            throw new ServiceException("Database non inizializzato", 500);
        }

        if (noteId == null || noteId.isEmpty()) {
            throw new ServiceException("ID della nota mancante o non valido", 400);
        }

        if (!noteDB.getMap().containsKey(noteId)) {
            throw new ServiceException("Nota non trovata", 404);
        }

        Note noteToUpdate = noteDB.getMap().get(noteId);
        if (!noteToUpdate.getPermission().canEdit(userEmail, noteToUpdate)) {
            throw new ServiceException("L'utente " + userEmail + " non ha il permesso di aggiornare la nota " + noteId, 403);
        }

        Version newVersion;
        String[] newTags = null;
        Permission newPermission = null;

        try {
            JsonObject jsonObj = new JsonParser().parse(updateJson).getAsJsonObject();

            int lastKnownVersion = -1;
            if (jsonObj.has("lastKnownVersion")) {
                try {
                    lastKnownVersion = Integer.parseInt(jsonObj.get("lastKnownVersion").getAsString());
                } catch (NumberFormatException e) {
                    throw new ServiceException("Numero di versione non valido", 400);
                }
            }

            int currentVersionInDB = noteToUpdate.currentVersionNumber();
            System.out.println("Controllo versione:");
            System.out.println(" - Versione nota dal client (lastKnownVersion): " + lastKnownVersion);
            System.out.println(" - Versione attuale nel DB (currentVersionNumber): " + currentVersionInDB);

            if (lastKnownVersion != -1 && lastKnownVersion != currentVersionInDB) {
                throw new ServiceException("Nota modificata da un altro utente. Ricarica la pagina.", 409);
            }

            if (jsonObj.has("tags") && jsonObj.get("tags").isJsonArray()) {
                JsonArray tagsArray = jsonObj.getAsJsonArray("tags");
                newTags = new String[tagsArray.size()];
                for (int i = 0; i < tagsArray.size(); i++) {
                    newTags[i] = tagsArray.get(i).getAsString();
                }
            }

            if (jsonObj.has("permission") && !jsonObj.get("permission").isJsonNull()) {
                if (!noteToUpdate.isOwner(userEmail)) {
                    throw new ServiceException("Solo il proprietario può modificare i permessi", 403);
                }
                String permString = jsonObj.get("permission").getAsString();
                try {
                    newPermission = Permission.valueOf(permString.toUpperCase());
                } catch (IllegalArgumentException e) {
                    throw new ServiceException("Valore del permesso non valido: " + permString, 400);
                }
            }

            newVersion = VersionFactory.fromJson(updateJson);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Dati di aggiornamento non validi: " + e.getMessage(), 400);
        }

        if (newVersion == null || newVersion.getTitle() == null || newVersion.getTitle().isEmpty()) {
            throw new ServiceException("Titolo richiesto per la nuova versione", 400);
        }

        if (newTags != null) {
            for (String tag : newTags) {
                if (tag == null || tag.isEmpty()) {
                    throw new ServiceException("Nome del tag richiesto", 400);
                }
                if (!tagDB.getMap().containsKey(tag)) {
                    throw new ServiceException("Il tag " + tag + " non esiste", 400);
                }
            }
            noteToUpdate.setTags(newTags);
        }

        if (newPermission != null) {
            noteToUpdate.setPermission(newPermission);
        }

        noteToUpdate.newVersion(newVersion);
        noteDB.getMap().put(noteId, noteToUpdate);
        noteDB.commit();
    }

    public void closeDatabases() {
        if (noteDB != null) {
            noteDB.close();
        }
        if (tagDB != null) {
            tagDB.close();
        }
    }

}