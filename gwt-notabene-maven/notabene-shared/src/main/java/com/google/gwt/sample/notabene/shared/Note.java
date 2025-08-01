package com.google.gwt.sample.notabene.shared;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class Note implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String id;
    private String title;
    private String content;
    private String ownerUsername;
    private Date createdAt;
    private Date lastModified;
    private String folderId;
    private Set<String> tags;
    private NotePermission permission;
    private Set<String> readOnlyUsers;
    private Set<String> writeUsers;
    
    public Note() {
        this.tags = new HashSet<>();
        this.readOnlyUsers = new HashSet<>();
        this.writeUsers = new HashSet<>();
        this.permission = NotePermission.PRIVATE;
        this.createdAt = new Date();
        this.lastModified = new Date();
    }
    
    public Note(String title, String content, String ownerUsername) {
        this();
        this.title = title;
        this.content = content;
        this.ownerUsername = ownerUsername;
        this.id = generateId();
    }
    
    private String generateId() {
        return ownerUsername + "_" + System.currentTimeMillis();
    }
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getContent() { return content; }
    public void setContent(String content) { 
        this.content = content;
        this.lastModified = new Date();
    }
    
    public String getOwnerUsername() { return ownerUsername; }
    public void setOwnerUsername(String ownerUsername) { this.ownerUsername = ownerUsername; }
    
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    
    public Date getLastModified() { return lastModified; }
    public void setLastModified(Date lastModified) { this.lastModified = lastModified; }
    
    public String getFolderId() { return folderId; }
    public void setFolderId(String folderId) { this.folderId = folderId; }
    
    public Set<String> getTags() { return tags; }
    public void setTags(Set<String> tags) { this.tags = tags; }
    
    public NotePermission getPermission() { return permission; }
    public void setPermission(NotePermission permission) { this.permission = permission; }
    
    public Set<String> getReadOnlyUsers() { return readOnlyUsers; }
    public void setReadOnlyUsers(Set<String> readOnlyUsers) { this.readOnlyUsers = readOnlyUsers; }
    
    public Set<String> getWriteUsers() { return writeUsers; }
    public void setWriteUsers(Set<String> writeUsers) { this.writeUsers = writeUsers; }
    
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
}