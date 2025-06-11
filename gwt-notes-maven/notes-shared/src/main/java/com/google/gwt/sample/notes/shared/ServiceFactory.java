package com.google.gwt.sample.notes.shared;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

public class ServiceFactory {

    public static AuthServiceAsync getAuthService() {
        AuthServiceAsync service = GWT.create(AuthService.class);
        ((ServiceDefTarget) service).setServiceEntryPoint(GWT.getModuleBaseURL() + "api/login");
        return service;
    }
}
