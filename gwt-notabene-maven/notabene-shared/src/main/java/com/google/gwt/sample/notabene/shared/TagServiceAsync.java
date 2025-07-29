package com.google.gwt.sample.notabene.shared;

import com.google.gwt.user.client.rpc.AsyncCallback;
import java.util.List;

//Interfaccia asincrona per TagService utilizzata dal client GWT
public interface TagServiceAsync {
    
    void createTag(Tag tag, AsyncCallback<Boolean> callback);

    void getAllTags(AsyncCallback<List<Tag>> callback);
 
    void deleteTag(String tagName, AsyncCallback<Boolean> callback);

    void isTagNameExists(String tagName, AsyncCallback<Boolean> callback);
}
