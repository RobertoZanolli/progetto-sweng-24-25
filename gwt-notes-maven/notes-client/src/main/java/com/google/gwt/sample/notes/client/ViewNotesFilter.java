package com.google.gwt.sample.notes.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import com.google.gwt.sample.notes.shared.Note;

/**
 * Filtra le note in base a vari criteri
 */
public class ViewNotesFilter {

    /**
     * Filtra la lista di note in base ai criteri specificati
     * @param notes
     * @param keyword Parola chiave per filtrare titolo e contenuto delle note
     * @param selectedTags Lista di tag selezionati 
     * @param startModDate Data di inizio per il filtro sulla data di modifica
     * @param endModDate Data di fine per il filtro sulla data di modifica
     * @param startCreatedDate Data di inizio per il filtro sulla data di creazione
     * @param endCreatedDate Data di fine per il filtro sulla data di creazione
     * @return Lista filtrata di note che soddisfano tutti i criteri specificati
     */
    public static List<Note> filter(List<Note> notes,
                                    String keyword,
                                    List<String> selectedTags,
                                    Date startModDate,
                                    Date endModDate,
                                    Date startCreatedDate,
                                    Date endCreatedDate) {
        if (notes == null) {
            return new ArrayList<>();
        }
        String lowerKeyword = keyword == null ? "" : keyword.toLowerCase();

        return notes.stream()
            .filter(n -> {
                // Filtro per parola chiave
                String title = n.getCurrentVersion().getTitle() != null
                        ? n.getCurrentVersion().getTitle().toLowerCase()
                        : "";
                String content = n.getCurrentVersion().getContent() != null
                        ? n.getCurrentVersion().getContent().toLowerCase()
                        : "";
                boolean matchesKeyword = lowerKeyword.isEmpty()
                        || title.contains(lowerKeyword)
                        || content.contains(lowerKeyword);

                // Filtro per tag
                String[] noteTags = n.getTags() != null ? n.getTags() : new String[0];
                boolean matchesTags = selectedTags == null || selectedTags.isEmpty()
                        || selectedTags.stream().anyMatch(
                            tag -> java.util.Arrays.stream(noteTags)
                                    .anyMatch(t -> t.equalsIgnoreCase(tag))
                        );

                // Filtro per data di modifica
                boolean matchesModDate = true;
                Date updatedAt = n.getCurrentVersion().getUpdatedAt();
                if (startModDate != null && endModDate != null) {
                    matchesModDate = updatedAt != null
                            && !updatedAt.before(startModDate)
                            && !updatedAt.after(endModDate);
                }

                // Filtro per data di creazione
                boolean matchesCreatedDate = true;
                Date createdAt = n.getCreatedAt();
                if (startCreatedDate != null && endCreatedDate != null) {
                    matchesCreatedDate = createdAt != null
                            && !createdAt.before(startCreatedDate)
                            && !createdAt.after(endCreatedDate);
                }

                return matchesKeyword && matchesTags && matchesModDate && matchesCreatedDate;
            })
            .collect(Collectors.toList());
    }
}
