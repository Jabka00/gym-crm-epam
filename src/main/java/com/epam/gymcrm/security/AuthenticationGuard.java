package com.epam.gymcrm.security;

import com.epam.gymcrm.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthenticationGuard {

    private final AuthenticationService authenticationService;

    public void ensureAuthenticated(Credentials credentials) {
        authenticationService.requireAuthenticated(credentials);
    }
}
