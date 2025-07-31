package com.google.gwt.sample.notabene.shared;

import java.io.Serializable;

public enum NotePermission implements Serializable {
    PRIVATE("Privata"),
    READ_ONLY("Sola lettura"),
    READ_WRITE("Lettura e scrittura");
    
    private final String displayName;
    
    NotePermission(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}