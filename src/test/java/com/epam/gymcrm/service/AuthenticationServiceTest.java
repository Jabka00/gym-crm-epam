package com.epam.gymcrm.service;

import com.epam.gymcrm.repository.UserAuthenticationRepository;
import com.epam.gymcrm.util.DtoValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserAuthenticationRepository userAuthenticationRepository;

    @Spy
    private DtoValidator dtoValidator = new DtoValidator();

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    void shouldAuthenticateActiveUser() {
        when(userAuthenticationRepository.authenticate("Alice.Walker", "secret1234")).thenReturn(true);

        assertThat(authenticationService.authenticate("Alice.Walker", "secret1234")).isTrue();
        verify(userAuthenticationRepository, times(1)).authenticate("Alice.Walker", "secret1234");
    }

    @Test
    void shouldRejectInactiveUser() {
        when(userAuthenticationRepository.authenticate("Inactive.User", "secret1234")).thenReturn(false);

        assertThat(authenticationService.authenticate("Inactive.User", "secret1234")).isFalse();
        verify(userAuthenticationRepository, times(1)).authenticate("Inactive.User", "secret1234");
    }

    @Test
    void shouldAuthenticateActiveTrainee() {
        when(userAuthenticationRepository.authenticateTrainee("Kate.Doe", "secret1234")).thenReturn(true);

        assertThat(authenticationService.authenticateTrainee("Kate.Doe", "secret1234")).isTrue();
        verify(userAuthenticationRepository, times(1)).authenticateTrainee("Kate.Doe", "secret1234");
    }

    @Test
    void shouldRejectTrainerCredentialsForTraineeAuthentication() {
        when(userAuthenticationRepository.authenticateTrainee("John.Smith", "pass1234AB")).thenReturn(false);

        assertThat(authenticationService.authenticateTrainee("John.Smith", "pass1234AB")).isFalse();
        verify(userAuthenticationRepository, times(1)).authenticateTrainee("John.Smith", "pass1234AB");
    }

    @Test
    void shouldAuthenticateActiveTrainer() {
        when(userAuthenticationRepository.authenticateTrainer("John.Smith", "pass1234AB")).thenReturn(true);

        assertThat(authenticationService.authenticateTrainer("John.Smith", "pass1234AB")).isTrue();
        verify(userAuthenticationRepository, times(1)).authenticateTrainer("John.Smith", "pass1234AB");
    }

    @Test
    void shouldRejectTraineeCredentialsForTrainerAuthentication() {
        when(userAuthenticationRepository.authenticateTrainer("Kate.Doe", "secret1234")).thenReturn(false);

        assertThat(authenticationService.authenticateTrainer("Kate.Doe", "secret1234")).isFalse();
        verify(userAuthenticationRepository, times(1)).authenticateTrainer("Kate.Doe", "secret1234");
    }

    @Test
    void shouldRejectBlankUsernameForTraineeAuthentication() {
        assertThatThrownBy(() -> authenticationService.authenticateTrainee("", "secret1234"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Username cannot be null or empty");

        verify(userAuthenticationRepository, never()).authenticateTrainee(any(), any());
    }

    @Test
    void shouldRejectBlankPasswordForTrainerAuthentication() {
        assertThatThrownBy(() -> authenticationService.authenticateTrainer("John.Smith", ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password cannot be null or empty");

        verify(userAuthenticationRepository, never()).authenticateTrainer(any(), any());
    }

    @Test
    void shouldRejectBlankUsernameForAuthentication() {
        assertThatThrownBy(() -> authenticationService.authenticate("", "secret1234"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Username cannot be null or empty");

        verify(userAuthenticationRepository, never()).authenticate(any(), any());
    }
}
