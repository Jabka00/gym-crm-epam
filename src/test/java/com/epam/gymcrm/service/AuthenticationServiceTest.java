package com.epam.gymcrm.service;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
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

    @InjectMocks
    private AuthenticationService authenticationService;

    private void stubQuery(long count) {
        lenient().when(sessionFactory.getCurrentSession()).thenReturn(session);
        lenient().when(session.createQuery(anyString(), eq(Long.class))).thenReturn(query);
        lenient().when(query.setParameter(anyString(), anyString())).thenReturn(query);
        when(query.getSingleResult()).thenReturn(count);
    }

    @Test
    void shouldMatchActiveTraineeCredentials() {
        stubQuery(1L);

        assertThat(authenticationService.matchesTraineeCredentials("Kate.Doe", VALID_PASSWORD)).isTrue();
    }

    @Test
    void shouldNotMatchInactiveTraineeCredentials() {
        stubQuery(0L);

        assertThat(authenticationService.matchesTraineeCredentials("Inactive.User", VALID_PASSWORD)).isFalse();
    }

    @Test
    void shouldNotMatchTrainerCredentialsForTrainee() {
        stubQuery(0L);

        assertThat(authenticationService.matchesTraineeCredentials("John.Smith", TRAINER_PASSWORD)).isFalse();
    }

    @Test
    void shouldMatchActiveTrainerCredentials() {
        stubQuery(1L);

        assertThat(authenticationService.matchesTrainerCredentials("John.Smith", TRAINER_PASSWORD)).isTrue();
    }

    @Test
    void shouldNotMatchInactiveTrainerCredentials() {
        stubQuery(0L);

        assertThat(authenticationService.matchesTrainerCredentials("Inactive.Trainer", TRAINER_PASSWORD)).isFalse();
    }

    @Test
    void shouldNotMatchTraineeCredentialsForTrainer() {
        stubQuery(0L);

        assertThat(authenticationService.matchesTrainerCredentials("Kate.Doe", VALID_PASSWORD)).isFalse();
    }

    @Test
    void shouldReturnFalseForBlankUsernameWithoutQuery() {
        assertThat(authenticationService.matchesTraineeCredentials("", VALID_PASSWORD)).isFalse();
        assertThat(authenticationService.matchesTrainerCredentials(" ", TRAINER_PASSWORD)).isFalse();

        verify(sessionFactory, never()).getCurrentSession();
    }

    @Test
    void shouldReturnFalseForBlankPasswordWithoutQuery() {
        assertThat(authenticationService.matchesTraineeCredentials("Kate.Doe", "")).isFalse();
        assertThat(authenticationService.matchesTrainerCredentials("John.Smith", null)).isFalse();

        verify(sessionFactory, never()).getCurrentSession();
    }
}
