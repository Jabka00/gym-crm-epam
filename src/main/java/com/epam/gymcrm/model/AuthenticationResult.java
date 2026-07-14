package com.epam.gymcrm.model;

public enum AuthenticationResult {
    SUCCESS,
    FAILURE;

    public boolean isSuccess() {
        return this == SUCCESS;
    }

    public static AuthenticationResult from(boolean authenticated) {
        return authenticated ? SUCCESS : FAILURE;
    }
}
