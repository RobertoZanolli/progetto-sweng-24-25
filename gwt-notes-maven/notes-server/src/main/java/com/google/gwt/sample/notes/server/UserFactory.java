package com.google.gwt.sample.notes.server;

import com.google.gwt.sample.notes.shared.User;
import com.google.gwt.sample.notes.shared.ConcreteUser;

public class UserFactory {

    private static final JsonParser parser = new GsonJsonParser();
    private static UserFactory instance;

    private UserFactory() {}

    public static synchronized UserFactory getInstance() {
        if (instance == null) {
            instance = new UserFactory();
        }
        return instance;
    }

    public static synchronized User create(String email, String password) {
        return new ConcreteUser(email, password);
    }

    public static synchronized User fromJson(String json) {
        return parser.fromJson(json, ConcreteUser.class);
    }

    public static synchronized String toJson(User user) {
        return parser.toJson(user);
    }
}
