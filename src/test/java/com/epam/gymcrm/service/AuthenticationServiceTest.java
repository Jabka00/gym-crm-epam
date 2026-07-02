package com.epam.gymcrm.service;

import com.epam.gymcrm.repository.UserAuthenticationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserAuthenticationRepository userAuthenticationRepository;

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
}
