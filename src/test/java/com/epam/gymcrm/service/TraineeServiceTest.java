package com.epam.gymcrm.service;

import com.epam.gymcrm.dto.CreateTraineeRequest;
import com.epam.gymcrm.dto.TraineeResponse;
import com.epam.gymcrm.dto.UpdateTraineeRequest;
import com.epam.gymcrm.dto.UserInfo;
import com.epam.gymcrm.entity.TraineeEntity;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.exception.InvalidOperationException;
import com.epam.gymcrm.mapper.TraineeMapper;
import com.epam.gymcrm.repository.TraineeRepository;
import com.epam.gymcrm.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TraineeServiceTest {

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private CredentialGenerator credentialGenerator;

    @Spy
    private TraineeMapper traineeMapper = new TraineeMapper();

    @InjectMocks
    private TraineeService traineeService;

    @Test
    void shouldCreateTraineeWithGeneratedCredentials() {
        when(traineeRepository.findAll()).thenAnswer(inv -> Stream.empty());
        traineeService.initIdSequence();

        when(credentialGenerator.generateUsername(eq("Alice"), eq("Walker"), any(ConcurrentHashMap.class)))
                .thenReturn("Alice.Walker");
        when(credentialGenerator.generatePassword()).thenReturn("abcdefghij");
        when(traineeRepository.save(any(TraineeEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        CreateTraineeRequest request = new CreateTraineeRequest(
                new UserInfo("Alice", "Walker"), LocalDate.of(1995, 4, 12), "Kyiv");

        TraineeResponse created = traineeService.create(request);

        TraineeResponse expected = new TraineeResponse(
                1L, "Alice Walker", "Alice.Walker", LocalDate.of(1995, 4, 12), "Kyiv");
        assertThat(created).isEqualTo(expected);

        ArgumentCaptor<TraineeEntity> captor = ArgumentCaptor.forClass(TraineeEntity.class);
        verify(traineeRepository, times(1)).save(captor.capture());
        assertThat(captor.getValue().getUsername()).isEqualTo("Alice.Walker");
        assertThat(captor.getValue().getPassword()).isEqualTo("abcdefghij");
        assertThat(captor.getValue().isActive()).isTrue();
        verify(credentialGenerator, times(1)).generatePassword();
    }

    @Test
    void shouldUpdateExistingTraineeWithoutRegeneratingUsernameWhenNameUnchanged() {
        TraineeEntity existing = TestDataFactory.createTraineeWithCredentials();
        existing.setUserId(1L);
        when(traineeRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(traineeRepository.save(any(TraineeEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateTraineeRequest request = new UpdateTraineeRequest(
                1L, new UserInfo("Alice", "Walker"), LocalDate.of(1995, 4, 12), "Lviv", true);

        TraineeResponse updated = traineeService.update(request);

        TraineeResponse expected = new TraineeResponse(
                1L, "Alice Walker", "Alice.Walker", LocalDate.of(1995, 4, 12), "Lviv");
        assertThat(updated).isEqualTo(expected);
        verify(credentialGenerator, never()).generateUsername(any(), any(), any());
        verify(traineeRepository, times(1)).save(existing);
    }

    @Test
    void shouldRegenerateUsernameWhenNameChangesOnUpdate() {
        TraineeEntity existing = TestDataFactory.createTraineeWithCredentials();
        existing.setUserId(1L);
        when(traineeRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(traineeRepository.save(any(TraineeEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(credentialGenerator.generateUsername(eq("Alice"), eq("Cooper"), any(ConcurrentHashMap.class)))
                .thenReturn("Alice.Cooper");

        UpdateTraineeRequest request = new UpdateTraineeRequest(
                1L, new UserInfo("Alice", "Cooper"), LocalDate.of(1995, 4, 12), "Kyiv", true);

        TraineeResponse updated = traineeService.update(request);

        TraineeResponse expected = new TraineeResponse(
                1L, "Alice Cooper", "Alice.Cooper", LocalDate.of(1995, 4, 12), "Kyiv");
        assertThat(updated).isEqualTo(expected);
        verify(credentialGenerator, times(1))
                .generateUsername(eq("Alice"), eq("Cooper"), any(ConcurrentHashMap.class));
    }

    @Test
    void shouldThrowWhenUpdatingMissingTrainee() {
        when(traineeRepository.findById(99L)).thenReturn(Optional.empty());

        UpdateTraineeRequest request = new UpdateTraineeRequest(
                99L, new UserInfo("Alice", "Walker"), LocalDate.of(1995, 4, 12), "Kyiv", true);

        assertThatThrownBy(() -> traineeService.update(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");

        verify(traineeRepository, never()).save(any());
    }

    @Test
    void shouldDeleteExistingTrainee() {
        TraineeEntity trainee = TestDataFactory.createTraineeWithCredentials();
        trainee.setUserId(1L);
        when(traineeRepository.findById(1L)).thenReturn(Optional.of(trainee));

        traineeService.delete(1L);

        verify(traineeRepository, times(1)).delete(1L);
    }

    @Test
    void shouldThrowWhenDeletingMissingTrainee() {
        when(traineeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> traineeService.delete(1L))
                .isInstanceOf(EntityNotFoundException.class);

        verify(traineeRepository, never()).delete(1L);
    }

    @Test
    void shouldGetTraineeById() {
        TraineeEntity trainee = TestDataFactory.createTraineeWithCredentials();
        trainee.setUserId(1L);
        when(traineeRepository.findById(1L)).thenReturn(Optional.of(trainee));

        TraineeResponse response = traineeService.getById(1L);

        TraineeResponse expected = new TraineeResponse(
                1L, "Alice Walker", "Alice.Walker", LocalDate.of(1995, 4, 12), "Kyiv");
        assertThat(response).isEqualTo(expected);
    }

    @Test
    void shouldFindAllTrainees() {
        TraineeEntity trainee = TestDataFactory.createTraineeWithCredentials();
        trainee.setUserId(1L);
        when(traineeRepository.findAll()).thenAnswer(inv -> Stream.of(trainee));

        List<TraineeResponse> all = traineeService.findAll();

        TraineeResponse expected = new TraineeResponse(
                1L, "Alice Walker", "Alice.Walker", LocalDate.of(1995, 4, 12), "Kyiv");
        assertThat(all).containsExactly(expected);
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
        TraineeEntity trainee = TestDataFactory.createTraineeWithCredentials();
        trainee.setUserId(1L);
        trainee.setActive(true);
        when(traineeRepository.findById(1L)).thenReturn(Optional.of(trainee));

        TraineeResponse response = traineeService.getActiveById(1L);

        TraineeResponse expected = new TraineeResponse(
                1L, "Alice Walker", "Alice.Walker", LocalDate.of(1995, 4, 12), "Kyiv");
        assertThat(response).isEqualTo(expected);
    }

    @Test
    void shouldThrowWhenTraineeInactive() {
        TraineeEntity trainee = TestDataFactory.createTraineeWithCredentials();
        trainee.setUserId(1L);
        trainee.setActive(false);
        when(traineeRepository.findById(1L)).thenReturn(Optional.of(trainee));

        assertThatThrownBy(() -> traineeService.getActiveById(1L))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("inactive");
    }
}
