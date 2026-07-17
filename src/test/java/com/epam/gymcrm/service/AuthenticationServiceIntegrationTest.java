package com.epam.gymcrm.service;

import com.epam.gymcrm.support.MySqlIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@MySqlIntegrationTest
class AuthenticationServiceIntegrationTest {

    @Autowired
    private AuthenticationService authenticationService;

    @Test
    void shouldMatchSeedTraineeCredentials() {
        assertThat(authenticationService.matchesTraineeCredentials("Alice.Walker", "qW3eRt5yUi")).isTrue();
    }

    @Test
    void shouldMatchSeedTrainerCredentials() {
        assertThat(authenticationService.matchesTrainerCredentials("John.Smith", "pass1234AB")).isTrue();
    }

    @Test
    void shouldRejectWrongPasswordForTrainee() {
        assertThat(authenticationService.matchesTraineeCredentials("Alice.Walker", "WrongPass1")).isFalse();
    }

    @Test
    void shouldRejectTrainerCredentialsForTraineeAuthentication() {
        assertThat(authenticationService.matchesTraineeCredentials("John.Smith", "pass1234AB")).isFalse();
    }

    @Test
    void shouldRejectInactiveTraineeCredentials() {
        assertThat(authenticationService.matchesTraineeCredentials("Carol.White", "Hn2jKx9cVb")).isFalse();
    }

    @Test
    void shouldRejectInactiveTrainerCredentials() {
        assertThat(authenticationService.matchesTrainerCredentials("Mike.Brown", "Tz7nVbCx4R")).isFalse();
    }
}
