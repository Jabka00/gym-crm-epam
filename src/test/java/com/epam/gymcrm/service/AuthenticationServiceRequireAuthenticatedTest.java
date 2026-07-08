package com.epam.gymcrm.service;

import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.repository.UserAuthenticationRepository;
import com.epam.gymcrm.security.Credentials;
import com.epam.gymcrm.util.DtoValidator;
import jakarta.validation.Validation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceRequireAuthenticatedTest {

    @Mock
    private UserAuthenticationRepository userAuthenticationRepository;

    private DtoValidator dtoValidator;
    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        dtoValidator = new DtoValidator(Validation.buildDefaultValidatorFactory().getValidator());
        authenticationService = new AuthenticationService(userAuthenticationRepository, dtoValidator);
    }

    @Test
    void shouldRequireAuthenticatedWhenCredentialsAreValid() {
        Credentials credentials = new Credentials("Alice.Walker", "secret1234");
        when(userAuthenticationRepository.authenticate("Alice.Walker", "secret1234")).thenReturn(true);

        assertThatCode(() -> authenticationService.requireAuthenticated(credentials))
                .doesNotThrowAnyException();

        verify(userAuthenticationRepository, times(1)).authenticate("Alice.Walker", "secret1234");
    }

    @Test
    void shouldThrowWhenCredentialsAreInvalid() {
        Credentials credentials = new Credentials("Inactive.User", "secret1234");
        when(userAuthenticationRepository.authenticate("Inactive.User", "secret1234")).thenReturn(false);

        assertThatThrownBy(() -> authenticationService.requireAuthenticated(credentials))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Invalid credentials for username: Inactive.User");

        verify(userAuthenticationRepository, times(1)).authenticate("Inactive.User", "secret1234");
    }
}
