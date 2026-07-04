package com.epam.gymcrm.security;

public record Credentials(String username, String password) {

    public Credentials {
        validate(username, password);
    }

    public static void validate(String username, String password) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
    }
}
