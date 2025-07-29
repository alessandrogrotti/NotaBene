package com.google.gwt.sample.notabene.shared;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import java.util.List;

//gestione operazioni sui tag
@RemoteServiceRelativePath("tagService")
public interface TagService extends RemoteService {
    
    boolean createTag(Tag tag) throws IllegalArgumentException;

    List<Tag> getAllTags();

    boolean deleteTag(String tagName) throws IllegalArgumentException;
    
    boolean isTagNameExists(String tagName);
}
