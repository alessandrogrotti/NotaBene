package com.google.gwt.sample.notabene.shared;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.LinkedList;
import java.util.List;

public class Note implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String id;
    private String title;
    private String content;
    private String ownerUsername;
    private Date createdAt;
    private Date lastModified;
    private Set<String> tags;
    private NotePermission permission;
    private Set<String> readOnlyUsers;
    private Set<String> writeUsers;
    private String currentVersionId;
    private int versionNumber;
    private String editorUsername;
    private LinkedList<NoteVersion> versions;

    public Note() {
        this.tags = new HashSet<>();
        this.readOnlyUsers = new HashSet<>();
        this.writeUsers = new HashSet<>();
        this.permission = NotePermission.PRIVATE;
        this.createdAt = new Date();
        this.lastModified = new Date();
        this.versions = new LinkedList<>();
        this.versionNumber = 0;
    }
    
    public Note(String title, String content, String ownerUsername) {
        this();
        this.title = title;
        this.content = content;
        this.ownerUsername = ownerUsername;
        this.id = generateId();
    }
    
    private String generateId() {
        return ownerUsername + "_" + System.currentTimeMillis() + Math.random();
    }
    
    public String getId() { return id; }

    public void setId(String id) { 
        this.id = id; 
    }
    
    public String getTitle() { return title; }

    public void setTitle(String title) { 
        this.title = title;
        this.lastModified = new Date();
    }

    public String getContent() { return content; }

    public void setContent(String content) { 
        this.content = content;
        this.lastModified = new Date();
    }
    
    public String getOwnerUsername() { return ownerUsername; }

    public void setOwnerUsername(String ownerUsername) { 
        this.ownerUsername = ownerUsername; 
    }

    public Date getCreatedAt() { return createdAt; }

    public void setCreatedAt(Date createdAt) { 
        this.createdAt = createdAt; 
    }

    public Date getLastModified() { return lastModified; }

    public void setLastModified(Date lastModified) { 
        this.lastModified = lastModified; 
    }

    public Set<String> getTags() { return tags; }

    public void setTags(Set<String> tags) { 
        this.tags = tags; 
    }

    public NotePermission getPermission() { return permission; }

    public void setPermission(NotePermission permission) { 
        this.permission = permission; 
    }

    public Set<String> getReadOnlyUsers() { return readOnlyUsers; }

    public void setReadOnlyUsers(Set<String> readOnlyUsers) { 
        this.readOnlyUsers = readOnlyUsers; 
    }

    public Set<String> getWriteUsers() { return writeUsers; }

    public void setWriteUsers(Set<String> writeUsers) { 
        this.writeUsers = writeUsers; 
    }

    public void addTag(String tag) {
        this.tags.add(tag);
        this.lastModified = new Date();
    }
    
    public void removeTag(String tag) {
        this.tags.remove(tag);
        this.lastModified = new Date();
    }
    
    public boolean canRead(String username) {
        if (ownerUsername.equals(username)) return true;
        if (permission == NotePermission.PRIVATE) return false;
        return readOnlyUsers.contains(username) || writeUsers.contains(username);
    }
    
    public boolean canWrite(String username) {
        if (ownerUsername.equals(username)) return true;
        if (permission == NotePermission.PRIVATE || permission == NotePermission.READ_ONLY) {
            return false;
        }
        return writeUsers.contains(username);
    }
    
    public NoteVersion createVersion() {
        return new NoteVersion(this);
    }
    
    public void addVersion(NoteVersion version) {
        if (versions == null) {
            versions = new LinkedList<>();
        }
        versions.addFirst(version);
    }
    
    public boolean hasVersionHistory() {
        return versions != null && !versions.isEmpty();
    }
    
    public int getVersionCount() {
        return versions != null ? versions.size() : 0;
    }
    
    public String getCurrentVersionId() { return currentVersionId; }

    public void setCurrentVersionId(String currentVersionId) { 
        this.currentVersionId = currentVersionId; 
    }
    
    public int getVersionNumber() { return versionNumber; }

    public void setVersionNumber(int versionNumber) { 
        this.versionNumber = versionNumber; 
    }

    public String getEditorUsername() { return editorUsername; }
    
    public void setEditorUsername(String editorUsername) { 
        this.editorUsername = editorUsername; 
    }
    
    public List<NoteVersion> getVersions() { 
        return versions != null ? new LinkedList<>(versions) : new LinkedList<>(); 
    }
    
    public void setVersions(LinkedList<NoteVersion> versions) { 
        this.versions = versions; 
    }

    public void markAsModified(String editorUsername) {
        this.lastModified = new Date();
        this.editorUsername = editorUsername;
    }
    
    public void incrementVersionNumber() {
        this.versionNumber++;
    }
}