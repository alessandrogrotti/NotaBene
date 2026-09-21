package com.google.gwt.sample.notabene.shared;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;
import java.util.HashSet;

public class NoteFilter implements Serializable {

    private static final long serialVersionUID = 1L;

    private String keyword;
    private Set<String> tags;
    private String author;
    private Date startDate;
    private Date endDate;
    private boolean searchInTitle;
    private boolean searchInContent;

    public NoteFilter() {
        this.tags = new HashSet<>();
        this.searchInTitle = true;
        this.searchInContent = true;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags != null ? tags : new HashSet<>();
    }

    public void addTag(String tag) {
        if (tag != null && !tag.trim().isEmpty()) {
            this.tags.add(tag);
        }
    }

    public void removeTag(String tag) {
        this.tags.remove(tag);
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public boolean isSearchInTitle() {
        return searchInTitle;
    }

    public void setSearchInTitle(boolean searchInTitle) {
        this.searchInTitle = searchInTitle;
    }

    public boolean isSearchInContent() {
        return searchInContent;
    }

    public void setSearchInContent(boolean searchInContent) {
        this.searchInContent = searchInContent;
    }

    public boolean isEmpty() {
        return (keyword == null || keyword.trim().isEmpty()) &&
               (tags == null || tags.isEmpty()) &&
               (author == null || author.trim().isEmpty()) &&
               startDate == null &&
               endDate == null;
    }

    public void clear() {
        this.keyword = null;
        this.tags.clear();
        this.author = null;
        this.startDate = null;
        this.endDate = null;
        this.searchInTitle = true;
        this.searchInContent = true;
    }
}
