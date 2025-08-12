package com.google.gwt.sample.notabene.shared;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class NoteVersion implements Serializable {
    
    private static final long serialVersionUID = 1L;
  
    private String versionId;
    private String noteId;
    private String title;
    private String content;
    private Set<String> tags;
    private int versionNumber;
    private Date versionCreatedAt;
    private String editorUsername;
    
    public NoteVersion() {
        this.tags = new HashSet<>();
        this.versionCreatedAt = new Date();
    }
    
    public NoteVersion(Note note, String editorUsername) {
        this();
        if (note == null) {
            throw new IllegalArgumentException("Note cannot be null");
        }
        this.noteId = note.getId();
        this.title = note.getTitle();
        this.content = note.getContent();
        this.tags = note.getTags() != null ? new HashSet<>(note.getTags()) : new HashSet<>();
        this.versionNumber = note.getVersionNumber();
        this.editorUsername = editorUsername;
        this.versionCreatedAt = note.getLastModified() != null ? new Date(note.getLastModified().getTime()) : new Date();
        this.versionId = generateVersionId(note.getId(), this.versionNumber);
    }
    
    private String generateVersionId(String noteId, int versionNumber) {
        return noteId + "_v" + versionNumber + "_" + System.currentTimeMillis();
    }
    
    public Note toReadOnlyNote() {
        Note readOnlyNote = new Note();
        readOnlyNote.setId(noteId);
        readOnlyNote.setTitle(title);
        readOnlyNote.setContent(content);
        readOnlyNote.setTags(tags != null ? new HashSet<>(tags) : new HashSet<>());
        
        readOnlyNote.setOwnerUsername(editorUsername);
        readOnlyNote.setCreatedAt(versionCreatedAt);
        readOnlyNote.setLastModified(versionCreatedAt);
        
        readOnlyNote.setReadOnlyUsers(new HashSet<>());
        readOnlyNote.setWriteUsers(new HashSet<>());
        
        return readOnlyNote;
    }
    
    public String getVersionId() { return versionId; }
    public void setVersionId(String versionId) { this.versionId = versionId; }
    
    public String getNoteId() { return noteId; }
    public void setNoteId(String noteId) { this.noteId = noteId; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public Set<String> getTags() { return tags; }
    public void setTags(Set<String> tags) { this.tags = tags; }
    
    public int getVersionNumber() { return versionNumber; }
    public void setVersionNumber(int versionNumber) { this.versionNumber = versionNumber; }
    
    public Date getVersionCreatedAt() { return versionCreatedAt; }
    public void setVersionCreatedAt(Date versionCreatedAt) { this.versionCreatedAt = versionCreatedAt; }
    
    public String getEditorUsername() { return editorUsername; }
    public void setEditorUsername(String editorUsername) { this.editorUsername = editorUsername; }
    
    @Override
    public String toString() {
        return "NoteVersion{" +
                "versionId='" + versionId + '\'' +
                ", noteId='" + noteId + '\'' +
                ", title='" + title + '\'' +
                ", versionNumber=" + versionNumber +
                ", versionCreatedAt=" + versionCreatedAt +
                ", editorUsername='" + editorUsername + '\'' +
                '}';
    }
}