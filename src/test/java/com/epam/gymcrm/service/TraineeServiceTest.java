package com.epam.gymcrm.service;

import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.exception.InvalidOperationException;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.repository.TraineeRepository;
import com.epam.gymcrm.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TraineeServiceTest {

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private CredentialGenerator credentialGenerator;

    @InjectMocks
    private TraineeService traineeService;

    @Test
    void shouldCreateTraineeWithGeneratedCredentials() {
        Trainee trainee = TestDataFactory.createDefaultTrainee();
        when(traineeRepository.findAll()).thenReturn(List.of());
        when(credentialGenerator.generateUsername(eq("Alice"), eq("Walker"), eq(Set.of())))
                .thenReturn("Alice.Walker");
        when(credentialGenerator.generatePassword()).thenReturn("abcdefghij");
        when(traineeRepository.save(any(Trainee.class))).thenAnswer(invocation -> {
            Trainee saved = invocation.getArgument(0);
            saved.setUserId(1L);
            return saved;
        });

        Trainee created = traineeService.create(trainee);

        assertThat(created.getUserId()).isEqualTo(1L);
        assertThat(created.getUsername()).isEqualTo("Alice.Walker");
        assertThat(created.getPassword()).isEqualTo("abcdefghij");
        assertThat(created.isActive()).isTrue();
        verify(traineeRepository).save(trainee);
    }

    @Test
    void shouldUpdateExistingTrainee() {
        Trainee trainee = TestDataFactory.createTraineeWithCredentials();
        trainee.setUserId(1L);
        trainee.setAddress("Lviv");
        when(traineeRepository.findById(1L)).thenReturn(Optional.of(trainee));
        when(traineeRepository.update(trainee)).thenReturn(trainee);

        Trainee updated = traineeService.update(trainee);

        assertThat(updated.getAddress()).isEqualTo("Lviv");
        verify(traineeRepository).update(trainee);
    }

    @Test
    void shouldThrowWhenUpdatingMissingTrainee() {
        Trainee trainee = TestDataFactory.createDefaultTrainee();
        trainee.setUserId(99L);
        when(traineeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> traineeService.update(trainee))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");

        verify(traineeRepository, never()).update(any());
    }

    @Test
    void shouldDeleteExistingTrainee() {
        Trainee trainee = TestDataFactory.createTraineeWithCredentials();
        trainee.setUserId(1L);
        when(traineeRepository.findById(1L)).thenReturn(Optional.of(trainee));

        traineeService.delete(1L);

        verify(traineeRepository).delete(1L);
    }

    @Test
    void shouldThrowWhenDeletingMissingTrainee() {
        when(traineeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> traineeService.delete(1L))
                .isInstanceOf(EntityNotFoundException.class);

        verify(traineeRepository, never()).delete(1L);
    }

    @Test
    void shouldFindTraineeById() {
        Trainee trainee = TestDataFactory.createTraineeWithCredentials();
        trainee.setUserId(1L);
        when(traineeRepository.findById(1L)).thenReturn(Optional.of(trainee));

        assertThat(traineeService.findById(1L)).contains(trainee);
    }

    @Test
    void shouldFindAllTrainees() {
        Trainee trainee = TestDataFactory.createTraineeWithCredentials();
        when(traineeRepository.findAll()).thenReturn(List.of(trainee));

        assertThat(traineeService.findAll()).containsExactly(trainee);
    }

    @Test
    void shouldThrowWhenGettingMissingTrainee() {
        when(traineeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> traineeService.getById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void shouldReturnActiveTrainee() {
        Trainee trainee = TestDataFactory.createTraineeWithCredentials();
        trainee.setUserId(1L);
        trainee.setActive(true);
        when(traineeRepository.findById(1L)).thenReturn(Optional.of(trainee));

        assertThat(traineeService.getActiveById(1L)).isSameAs(trainee);
    }

    @Test
    void shouldThrowWhenTraineeInactive() {
        Trainee trainee = TestDataFactory.createTraineeWithCredentials();
        trainee.setUserId(1L);
        trainee.setActive(false);
        when(traineeRepository.findById(1L)).thenReturn(Optional.of(trainee));

        assertThatThrownBy(() -> traineeService.getActiveById(1L))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("inactive");
    }
}
