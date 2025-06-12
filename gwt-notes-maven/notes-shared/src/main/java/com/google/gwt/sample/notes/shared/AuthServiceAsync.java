package com.google.gwt.sample.notes.shared;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface AuthServiceAsync {
    void login(String email, AsyncCallback<Void> callback);
    void getCurrentEmail(AsyncCallback<String> callback);
}
