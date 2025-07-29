package com.google.gwt.sample.notabene.shared;

import com.google.gwt.user.client.rpc.IsSerializable;
import java.io.Serializable;

public class Tag implements Serializable, IsSerializable {
    
    private static final long serialVersionUID = 1L;
    
    private String name;
    
    // Costruttore vuoto per GWT
    public Tag() {
    }

    public Tag(String name) {
        this.name = name;
    }
    
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    
    @Override
    public String toString() {
        return "Tag{" +
                ", name='" + name + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Tag tag = (Tag) obj;
        return name != null ? name.equalsIgnoreCase(tag.name) : tag.name == null;
    }
    
    @Override
    public int hashCode() {
        return name != null ? name.toLowerCase().hashCode() : 0;
    }
}
