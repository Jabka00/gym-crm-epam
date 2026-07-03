package com.epam.gymcrm.security;

import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.service.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthenticationGuardTest {

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private AuthenticationGuard authenticationGuard;

    @Test
    void shouldAllowAuthenticatedUser() {
        Credentials credentials = new Credentials("Alice.Walker", "secret1234");

        assertThatCode(() -> authenticationGuard.ensureAuthenticated(credentials))
                .doesNotThrowAnyException();

        verify(authenticationService, times(1)).requireAuthenticated(credentials);
    }

    @Test
    void shouldRejectInvalidCredentials() {
        Credentials credentials = new Credentials("Alice.Walker", "wrong");
        doThrow(new AuthenticationException("Invalid credentials for username: Alice.Walker"))
                .when(authenticationService)
                .requireAuthenticated(credentials);

        assertThatThrownBy(() -> authenticationGuard.ensureAuthenticated(credentials))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Invalid credentials for username: Alice.Walker");

        verify(authenticationService, times(1)).requireAuthenticated(credentials);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldRejectBlankUsername(String username) {
        assertThatThrownBy(() -> new Credentials(username, "secret1234"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Username cannot be null or empty");
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldRejectBlankPassword(String password) {
        assertThatThrownBy(() -> new Credentials("Alice.Walker", password))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password cannot be null or empty");
    }
}
