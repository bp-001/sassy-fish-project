package com.example.data_access;

public final class CurrentUserContext {

    private static String username = "sassy_user";

    private CurrentUserContext() {
    }

    public static String getUsername() {
        return username;
    }

    public static void setUsername(String username) {
        if (username == null || username.isBlank()) {
            return;
        }
        CurrentUserContext.username = username;
    }
}