package com.google.gwt.sample.notes.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import com.google.gwt.sample.notes.shared.Note;

public class ViewNotesFilter {
    /**
     * Filters the given list of notes based on the provided criteria.
     *
     * @param notes             the full list of notes to filter
     * @param keyword           the keyword for title/content search (case-insensitive)
     * @param selectedTags      the list of selected tags (case-insensitive)
     * @param startModDate      the start of the modification date range (inclusive), or null
     * @param endModDate        the end of the modification date range (inclusive), or null
     * @param startCreatedDate  the start of the creation date range (inclusive), or null
     * @param endCreatedDate    the end of the creation date range (inclusive), or null
     * @return                  a new list containing notes that match all criteria
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
                // Filtro per keyword
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
