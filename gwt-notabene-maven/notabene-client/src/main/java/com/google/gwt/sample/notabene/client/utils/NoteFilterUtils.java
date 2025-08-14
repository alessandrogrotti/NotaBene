package com.google.gwt.sample.notabene.client.utils;

import com.google.gwt.sample.notabene.shared.Note;
import com.google.gwt.sample.notabene.shared.NoteFilter;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;

public class NoteFilterUtils {

    public static List<Note> filterNotes(List<Note> notes, NoteFilter filter) {
        if (notes == null || filter == null || filter.isEmpty()) {
            return notes != null ? new ArrayList<>(notes) : new ArrayList<>();
        }
        List<Note> filteredNotes = new ArrayList<>();
        for (Note note : notes) {
            if (matchesFilter(note, filter)) {
                filteredNotes.add(note);
            }
        }
        return filteredNotes;
    }

    private static boolean matchesFilter(Note note, NoteFilter filter) {
        if (!matchesKeyword(note, filter)) {
            return false;
        }
        if (!matchesTags(note, filter)) {
            return false;
        }
        if (!matchesAuthor(note, filter)) {
            return false;
        }
        if (!matchesDateRange(note, filter)) {
            return false;
        }
        return true;
    }

    private static boolean matchesKeyword(Note note, NoteFilter filter) {
        String keyword = filter.getKeyword();
        if (keyword == null || keyword.trim().isEmpty()) {
            return true;
        }
        keyword = keyword.toLowerCase().trim();
        boolean matchFound = false;
        if (filter.isSearchInTitle() && note.getTitle() != null) {
            String title = note.getTitle().toLowerCase();
            if (title.contains(keyword)) {
                matchFound = true;
            }
        }
        if (filter.isSearchInContent() && note.getContent() != null) {
            String content = note.getContent().toLowerCase();
            if (content.contains(keyword)) {
                matchFound = true;
            }
        }
        return matchFound;
    }

    private static boolean matchesTags(Note note, NoteFilter filter) {
        if (filter.getTags() == null || filter.getTags().isEmpty()) {
            return true;
        }
        if (note.getTags() == null || note.getTags().isEmpty()) {
            return false;
        }
        for (String requiredTag : filter.getTags()) {
            if (note.getTags().contains(requiredTag)) {
                return true;
            }
        }
        return false;
    }

    private static boolean matchesAuthor(Note note, NoteFilter filter) {
        String authorFilter = filter.getAuthor();
        if (authorFilter == null || authorFilter.trim().isEmpty()) {
            return true;
        }
        if (note.getOwnerUsername() == null) {
            return false;
        }
        return note.getOwnerUsername().toLowerCase().contains(authorFilter.toLowerCase().trim());
    }

    private static boolean matchesDateRange(Note note, NoteFilter filter) {
        Date startDate = filter.getStartDate();
        Date endDate = filter.getEndDate();
        if (startDate == null && endDate == null) {
            return true;
        }
        Date noteDate = note.getCreatedAt();
        if (noteDate == null) {
            return false;
        }
        if (startDate != null && noteDate.before(startDate)) {
            return false;
        }
        if (endDate != null) {
            Date endOfDay = new Date(endDate.getTime() + 24 * 60 * 60 * 1000);
            if (noteDate.after(endOfDay)) {
                return false;
            }
        }
        return true;
    }

    public static List<Note> sortByRelevance(List<Note> notes, NoteFilter filter) {
        if (notes == null || filter == null || filter.isEmpty()) {
            return notes != null ? new ArrayList<>(notes) : new ArrayList<>();
        }
        notes.sort((note1, note2) -> {
            Date date1 = note1.getLastModified() != null ? note1.getLastModified() : note1.getCreatedAt();
            Date date2 = note2.getLastModified() != null ? note2.getLastModified() : note2.getCreatedAt();
            if (date1 == null && date2 == null) return 0;
            if (date1 == null) return 1;
            if (date2 == null) return -1;
            return date2.compareTo(date1);
        });
        return notes;
    }
}
