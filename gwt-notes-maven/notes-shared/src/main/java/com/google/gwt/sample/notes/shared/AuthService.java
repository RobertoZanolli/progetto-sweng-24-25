package com.google.gwt.sample.notes.shared;

import com.google.gwt.user.client.rpc.RemoteService;

public interface AuthService extends RemoteService {
    void login(String email);
    String getCurrentEmail();
}
