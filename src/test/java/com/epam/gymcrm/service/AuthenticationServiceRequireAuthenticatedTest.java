package com.epam.gymcrm.service;

import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.dto.Credentials;
import com.epam.gymcrm.util.DtoValidator;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceRequireAuthenticatedTest {

    @Mock
    private SessionFactory sessionFactory;

    @Mock
    private Session session;

    @Mock
    private Query<Long> query;

    @Spy
    private DtoValidator dtoValidator = new DtoValidator();

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    void shouldRequireAuthenticatedWhenCredentialsAreValid() {
        Credentials credentials = new Credentials("Alice.Walker", "secret1234");
        when(sessionFactory.getCurrentSession()).thenReturn(session);
        when(session.createQuery(anyString(), eq(Long.class))).thenReturn(query);
        when(query.setParameter(anyString(), anyString())).thenReturn(query);
        when(query.getSingleResult()).thenReturn(1L);

        assertThatCode(() -> authenticationService.requireAuthenticated(credentials))
                .doesNotThrowAnyException();

        verify(session, times(1)).createQuery(anyString(), eq(Long.class));
    }

    @Test
    void shouldThrowWhenCredentialsAreInvalid() {
        Credentials credentials = new Credentials("Inactive.User", "secret1234");
        when(sessionFactory.getCurrentSession()).thenReturn(session);
        when(session.createQuery(anyString(), eq(Long.class))).thenReturn(query);
        when(query.setParameter(anyString(), anyString())).thenReturn(query);
        when(query.getSingleResult()).thenReturn(0L);

        assertThatThrownBy(() -> authenticationService.requireAuthenticated(credentials))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Invalid credentials");

        verify(session, times(1)).createQuery(anyString(), eq(Long.class));
    }
}
