package com.epam.gymcrm.service;

import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.repository.UserAuthenticationRepository;
import com.epam.gymcrm.security.Credentials;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthenticationService {

    private final UserAuthenticationRepository userAuthenticationRepository;

    public boolean authenticate(String username, String password) {
        return userAuthenticationRepository.authenticate(username, password);
    }

    public boolean authenticateTrainee(String username, String password) {
        Credentials.validate(username, password);
        boolean authenticated = userAuthenticationRepository.authenticateTrainee(username, password);
        log.info("Trainee password verification for username={}: {}",
                username, authenticated ? "success" : "failed");
        return authenticated;
    }

    public boolean authenticateTrainer(String username, String password) {
        Credentials.validate(username, password);
        boolean authenticated = userAuthenticationRepository.authenticateTrainer(username, password);
        log.info("Trainer password verification for username={}: {}",
                username, authenticated ? "success" : "failed");
        return authenticated;
    }

    public void requireAuthenticated(Credentials credentials) {
        if (!authenticate(credentials.username(), credentials.password())) {
            throw new AuthenticationException(
                    "Invalid credentials for username: " + credentials.username());
        }
        log.debug("Authentication succeeded for username={}", credentials.username());
    }
}
