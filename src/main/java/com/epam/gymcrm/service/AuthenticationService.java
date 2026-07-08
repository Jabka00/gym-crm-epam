package com.epam.gymcrm.service;

import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.repository.UserAuthenticationRepository;
import com.epam.gymcrm.security.Credentials;
import com.epam.gymcrm.util.DtoValidator;
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
    private final DtoValidator dtoValidator;

    public boolean authenticate(String username, String password) {
        Credentials credentials = new Credentials(username, password);
        dtoValidator.validate(credentials);
        return userAuthenticationRepository.authenticate(credentials.username(), credentials.password());
    }

    public boolean authenticateTrainee(String username, String password) {
        Credentials credentials = new Credentials(username, password);
        dtoValidator.validate(credentials);
        boolean authenticated = userAuthenticationRepository.authenticateTrainee(
                credentials.username(), credentials.password());
        log.info("Trainee password verification for username={}: {}",
                credentials.username(), authenticated ? "success" : "failed");
        return authenticated;
    }

    public boolean authenticateTrainer(String username, String password) {
        Credentials credentials = new Credentials(username, password);
        dtoValidator.validate(credentials);
        boolean authenticated = userAuthenticationRepository.authenticateTrainer(
                credentials.username(), credentials.password());
        log.info("Trainer password verification for username={}: {}",
                credentials.username(), authenticated ? "success" : "failed");
        return authenticated;
    }

    public void requireAuthenticated(Credentials credentials) {
        dtoValidator.validate(credentials);
        if (!userAuthenticationRepository.authenticate(credentials.username(), credentials.password())) {
            throw new AuthenticationException(
                    "Invalid credentials for username: " + credentials.username());
        }
        log.debug("Authentication succeeded for username={}", credentials.username());
    }
}
