package com.google.gwt.sample.notes.server;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gwt.sample.notes.shared.Note;
import com.google.gwt.sample.notes.shared.Permission;
import com.google.gwt.sample.notes.shared.Version;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NoteService {

    private final NoteDB noteDB;
    private final TagDB tagDB;
    private final String noteLogName = "Note";
    private final String tagLogName = "Tag";

    public NoteService(NoteDB noteDB, TagDB tagDB) {
        this.noteDB = noteDB;
        this.tagDB = tagDB;
    }

    /**
     * Creates a new note.
     * @param noteJson The JSON string representation of the note.
     * @param userEmail The email of the user creating the note.
     * @throws NoteServiceException If an error occurs during note creation.
     */
    public void createNote(String noteJson, String userEmail) throws NoteServiceException {
        if (noteDB.getMap() == null || tagDB.getMap() == null) {
            throw new NoteServiceException("Database not initialized.", 500);
        }

        Note note;
        try {
            note = NoteFactory.fromJson(noteJson);
        } catch (Exception e) {
            throw new NoteServiceException("Invalid " + noteLogName + " data: " + e.getMessage(), 400);
        }

        if (note == null) {
            throw new NoteServiceException("Invalid note data", 400);
        }

        if (userEmail == null || userEmail.isEmpty()) {
            throw new NoteServiceException("Utente non autenticato", 401);
        }

        if (note.getOwnerEmail() == null || note.getOwnerEmail().isEmpty()) {
            note.setOwnerEmail(userEmail);
        } else if (!note.getOwnerEmail().equals(userEmail)) {
            throw new NoteServiceException("Owner email cannot be different from session email", 400);
        }

        if (note.getId() == null || note.getId().isEmpty()) {
            throw new NoteServiceException("Note ID required", 400);
        }

        if (noteDB.getMap().containsKey(note.getId())) {
            throw new NoteServiceException(noteLogName + " already exists", 409);
        }

        if (note.getAllVersions() == null || note.getAllVersions().isEmpty()) {
            throw new NoteServiceException("At least one version required", 400);
        }

        if (note.getCurrentVersion() == null || note.getCurrentVersion().getTitle() == null
                || note.getCurrentVersion().getTitle().isEmpty()) {
            throw new NoteServiceException("Title required", 400);
        }

        // Verify tags exist
        if (note.getTags() != null) {
            for (String tag : note.getTags()) {
                if (tag == null || tag.isEmpty()) {
                    throw new NoteServiceException(tagLogName + " name required", 400);
                }
                if (!tagDB.getMap().containsKey(tag)) {
                    throw new NoteServiceException("Tag " + tag + " does not exist", 400);
                }
            }
        }

        noteDB.getMap().put(note.getId(), note);
        noteDB.commit();
    }

    /**
     * Retrieves a specific note by its ID, checking user permissions.
     * @param noteId The ID of the note to retrieve.
     * @param userEmail The email of the user requesting the note.
     * @return The Note object.
     * @throws NoteServiceException If the note is not found, or the user is unauthorized/forbidden.
     */
    public Note getNoteById(String noteId, String userEmail) throws NoteServiceException {
        if (userEmail == null || userEmail.isEmpty()) {
            throw new NoteServiceException("Utente non autenticato", 401);
        }

        if (noteId == null || noteId.isEmpty()) {
            throw new NoteServiceException("Note ID required", 400);
        }

        if (noteDB.getMap() == null) {
            throw new NoteServiceException("Notes database not initialized.", 500);
        }

        Note note = noteDB.getMap().get(noteId);
        if (note == null) {
            throw new NoteServiceException("Note with ID " + noteId + " not found", 404);
        }

        if (!note.getPermission().canView(userEmail, note)) {
            throw new NoteServiceException("User " + userEmail + " does not have permission to view note " + noteId, 403);
        }
        return note;
    }

    /**
     * Retrieves all notes visible to a specific user.
     * @param userEmail The email of the user requesting the notes.
     * @return A list of visible Note objects.
     * @throws NoteServiceException If the user is not authenticated or the database is not initialized.
     */
    public List<Note> getAllVisibleNotes(String userEmail) throws NoteServiceException {
        if (userEmail == null || userEmail.isEmpty()) {
            throw new NoteServiceException("Utente non autenticato", 401);
        }

        if (noteDB.getMap() == null) {
            throw new NoteServiceException("Notes database not initialized.", 500);
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
     * Deletes a note by its ID, checking user permissions.
     * @param noteId The ID of the note to delete.
     * @param userEmail The email of the user requesting the deletion.
     * @throws NoteServiceException If the note is not found, or the user is unauthorized/forbidden.
     */
    public void deleteNote(String noteId, String userEmail) throws NoteServiceException {
        // Removed authentication block as per instructions

        if (noteDB.getMap() == null) {
            throw new NoteServiceException("Notes database not initialized.", 500);
        }

        if (noteId == null || noteId.isEmpty()) {
            throw new NoteServiceException("Note ID required", 400);
        }

        if (!noteDB.getMap().containsKey(noteId)) {
            throw new NoteServiceException(noteLogName + " with ID " + noteId + " not found", 404);
        }

        Note noteToDelete = noteDB.getMap().get(noteId);
        if (!noteToDelete.getOwnerEmail().equals(userEmail) && !noteToDelete.getPermission().canEdit(userEmail, noteToDelete)) {
            throw new NoteServiceException("User " + userEmail + " does not have permission to delete note " + noteId, 403);
        }

        noteDB.getMap().remove(noteId);
        noteDB.commit();
    }

    /**
     * Updates an existing note by adding a new version, tags, or changing permissions.
     * @param noteId The ID of the note to update.
     * @param updateJson The JSON string containing the new version data, tags, and/or permission.
     * @param userEmail The email of the user updating the note.
     * @throws NoteServiceException If an error occurs during the update process.
     */
    @SuppressWarnings("deprecation")
    public void updateNote(String noteId, String updateJson, String userEmail) throws NoteServiceException {
        // Removed authentication block as per instructions

        if (noteDB.getMap() == null || tagDB.getMap() == null) {
            throw new NoteServiceException("Database not initialized.", 500);
        }

        if (noteId == null || noteId.isEmpty()) {
            throw new NoteServiceException("Missing or invalid note ID", 400);
        }

        if (!noteDB.getMap().containsKey(noteId)) {
            throw new NoteServiceException("Note not found.", 404);
        }

        // Permission check before update
        Note noteToUpdate = noteDB.getMap().get(noteId);
        if (!noteToUpdate.getPermission().canEdit(userEmail, noteToUpdate)) {
            throw new NoteServiceException("User " + userEmail + " does not have permission to update note " + noteId, 403);
        }

        Version newVersion;
        String[] newTags = null;
        Permission newPermission = null;
        LocalDateTime lastKnownUpdate = null;
        String lastKnownUpdateStr = null;
        try {
            JsonObject jsonObj = new JsonParser().parse(updateJson).getAsJsonObject();

            lastKnownUpdateStr = jsonObj.has("lastKnownUpdate") ? jsonObj.get("lastKnownUpdate").getAsString() : null;
            if (lastKnownUpdateStr != null) {
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    lastKnownUpdate = LocalDateTime.parse(lastKnownUpdateStr, formatter);
                } catch (DateTimeParseException e) {
                    throw new NoteServiceException("Invalid date format for lastKnownUpdate: " + lastKnownUpdateStr, 400);
                }
            }

            Date dbUpdatedAt = noteToUpdate.getCurrentVersion().getUpdatedAt();
            LocalDateTime dbUpdatedAtLDT = LocalDateTime.parse(
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(dbUpdatedAt),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            );

            if (lastKnownUpdateStr != null && (lastKnownUpdate == null || !lastKnownUpdate.equals(dbUpdatedAtLDT))) {
                throw new NoteServiceException("Note modified by another user. Please reload.", 409);
            }

            if (jsonObj.has("tags") && jsonObj.get("tags").isJsonArray()) {
                JsonArray tagsArray = jsonObj.getAsJsonArray("tags");
                newTags = new String[tagsArray.size()];
                for (int i = 0; i < tagsArray.size(); i++) {
                    newTags[i] = tagsArray.get(i).getAsString();
                }
            }

            if (jsonObj.has("permission") && !jsonObj.get("permission").isJsonNull()) {
                String permString = jsonObj.get("permission").getAsString();
                try {
                    newPermission = Permission.valueOf(permString.toUpperCase());
                } catch (IllegalArgumentException e) {
                    throw new NoteServiceException("Invalid permission value: " + permString, 400);
                }
            }

            // NoteFactory.fromJson will attempt to parse the entire object as a Version
            // This assumes the JSON for update contains version details, otherwise it might need adjustments
            newVersion = VersionFactory.fromJson(updateJson);
        } catch (NoteServiceException e) { // Re-throw NoteServiceExceptions caught in try block
            throw e;
        } catch (Exception e) {
            throw new NoteServiceException("Invalid update data: " + e.getMessage(), 400);
        }

        if (newVersion == null || newVersion.getTitle() == null || newVersion.getTitle().isEmpty()) {
            throw new NoteServiceException("Title required for new version", 400);
        }

        if (newTags != null) {
            for (String tag : newTags) {
                if (tag == null || tag.isEmpty()) {
                    throw new NoteServiceException(tagLogName + " name required", 400);
                }
                if (!tagDB.getMap().containsKey(tag)) {
                    throw new NoteServiceException("Tag " + tag + " does not exist", 400);
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

    /**
     * Custom exception class for NoteService errors.
     */
    public static class NoteServiceException extends Exception {
        private final int statusCode;

        public NoteServiceException(String message, int statusCode) {
            super(message);
            this.statusCode = statusCode;
        }

        public int getStatusCode() {
            return statusCode;
        }
    }
}