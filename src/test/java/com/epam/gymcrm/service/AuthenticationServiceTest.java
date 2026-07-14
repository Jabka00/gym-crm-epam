package com.epam.gymcrm.service;

import com.epam.gymcrm.exception.ValidationException;
import com.epam.gymcrm.model.AuthenticationResult;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    private static final String VALID_PASSWORD = "Secret1234";
    private static final String TRAINER_PASSWORD = "Pass1234AB";

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

    private void stubSuccessfulQuery(long count) {
        lenient().when(sessionFactory.getCurrentSession()).thenReturn(session);
        lenient().when(session.createQuery(anyString(), eq(Long.class))).thenReturn(query);
        lenient().when(query.setParameter(anyString(), anyString())).thenReturn(query);
        when(query.getSingleResult()).thenReturn(count);
    }

    @Test
    void shouldAuthenticateActiveUser() {
        stubSuccessfulQuery(1L);

        assertThat(authenticationService.authenticate("Alice.Walker", VALID_PASSWORD))
                .isEqualTo(AuthenticationResult.SUCCESS);
        verify(session, times(1)).createQuery(anyString(), eq(Long.class));
    }

    @Test
    void shouldRejectInactiveUser() {
        stubSuccessfulQuery(0L);

        assertThat(authenticationService.authenticate("Inactive.User", VALID_PASSWORD))
                .isEqualTo(AuthenticationResult.FAILURE);
    }

    @Test
    void shouldAuthenticateActiveTrainee() {
        stubSuccessfulQuery(1L);

        assertThat(authenticationService.authenticateTrainee("Kate.Doe", VALID_PASSWORD))
                .isEqualTo(AuthenticationResult.SUCCESS);
    }

    @Test
    void shouldRejectTrainerCredentialsForTraineeAuthentication() {
        stubSuccessfulQuery(0L);

        assertThat(authenticationService.authenticateTrainee("John.Smith", TRAINER_PASSWORD))
                .isEqualTo(AuthenticationResult.FAILURE);
    }

    @Test
    void shouldAuthenticateActiveTrainer() {
        stubSuccessfulQuery(1L);

        assertThat(authenticationService.authenticateTrainer("John.Smith", TRAINER_PASSWORD))
                .isEqualTo(AuthenticationResult.SUCCESS);
    }

    @Test
    void shouldRejectTraineeCredentialsForTrainerAuthentication() {
        stubSuccessfulQuery(0L);

        assertThat(authenticationService.authenticateTrainer("Kate.Doe", VALID_PASSWORD))
                .isEqualTo(AuthenticationResult.FAILURE);
    }

    @Test
    void shouldRejectBlankUsernameForTraineeAuthentication() {
        assertThatThrownBy(() -> authenticationService.authenticateTrainee("", VALID_PASSWORD))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Username cannot be null or empty");

        verify(sessionFactory, never()).getCurrentSession();
    }

    @Test
    void shouldRejectBlankPasswordForTrainerAuthentication() {
        assertThatThrownBy(() -> authenticationService.authenticateTrainer("John.Smith", ""))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Password cannot be null or empty");

        verify(sessionFactory, never()).getCurrentSession();
    }

    @Test
    void shouldRejectBlankUsernameForAuthentication() {
        assertThatThrownBy(() -> authenticationService.authenticate("", VALID_PASSWORD))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Username cannot be null or empty");

        verify(sessionFactory, never()).getCurrentSession();
    }
}
