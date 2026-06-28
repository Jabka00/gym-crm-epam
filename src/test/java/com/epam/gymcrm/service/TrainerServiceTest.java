package com.epam.gymcrm.service;

import com.epam.gymcrm.dto.CreateTrainerRequest;
import com.epam.gymcrm.dto.TrainerResponse;
import com.epam.gymcrm.dto.UpdateTrainerRequest;
import com.epam.gymcrm.dto.UserInfo;
import com.epam.gymcrm.entity.TrainerEntity;
import com.epam.gymcrm.entity.TrainingType;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.exception.InvalidOperationException;
import com.epam.gymcrm.mapper.TrainerMapper;
import com.epam.gymcrm.repository.TrainerRepository;
import com.epam.gymcrm.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

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
class TrainerServiceTest {

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private CredentialGenerator credentialGenerator;

    @Spy
    private TrainerMapper trainerMapper = new TrainerMapper();

    @InjectMocks
    private TrainerService trainerService;

    @Test
    void shouldCreateTrainerWithGeneratedCredentials() {
        when(trainerRepository.findAll()).thenAnswer(inv -> Stream.empty());
        trainerService.initIdSequence();

        when(credentialGenerator.generateUsername(eq("John"), eq("Smith"), any(ConcurrentHashMap.class)))
                .thenReturn("John.Smith");
        when(credentialGenerator.generatePassword()).thenReturn("abcdefghij");
        when(trainerRepository.save(any(TrainerEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        CreateTrainerRequest request = new CreateTrainerRequest(
                new UserInfo("John", "Smith"), TrainingType.YOGA);

        TrainerResponse created = trainerService.create(request);

        TrainerResponse expected = new TrainerResponse(1L, "John Smith", "John.Smith", TrainingType.YOGA);
        assertThat(created).isEqualTo(expected);

        ArgumentCaptor<TrainerEntity> captor = ArgumentCaptor.forClass(TrainerEntity.class);
        verify(trainerRepository, times(1)).save(captor.capture());
        assertThat(captor.getValue().getUsername()).isEqualTo("John.Smith");
        assertThat(captor.getValue().getPassword()).isEqualTo("abcdefghij");
        assertThat(captor.getValue().isActive()).isTrue();
        verify(credentialGenerator, times(1)).generatePassword();
    }

    @Test
    void shouldUpdateTrainerWithoutRegeneratingUsernameWhenNameUnchanged() {
        TrainerEntity existing = TestDataFactory.createTrainerWithCredentials();
        existing.setUserId(1L);
        when(trainerRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(trainerRepository.save(any(TrainerEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateTrainerRequest request = new UpdateTrainerRequest(
                1L, new UserInfo("John", "Smith"), TrainingType.BOXING, true);

        TrainerResponse updated = trainerService.update(request);

        TrainerResponse expected = new TrainerResponse(1L, "John Smith", "John.Smith", TrainingType.BOXING);
        assertThat(updated).isEqualTo(expected);
        verify(credentialGenerator, never()).generateUsername(any(), any(), any());
        verify(trainerRepository, times(1)).save(existing);
    }

    @Test
    void shouldRegenerateUsernameWhenNameChangesOnUpdate() {
        TrainerEntity existing = TestDataFactory.createTrainerWithCredentials();
        existing.setUserId(1L);
        when(trainerRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(trainerRepository.save(any(TrainerEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(credentialGenerator.generateUsername(eq("John"), eq("Doe"), any(ConcurrentHashMap.class)))
                .thenReturn("John.Doe");

        UpdateTrainerRequest request = new UpdateTrainerRequest(
                1L, new UserInfo("John", "Doe"), TrainingType.YOGA, true);

        TrainerResponse updated = trainerService.update(request);

        TrainerResponse expected = new TrainerResponse(1L, "John Doe", "John.Doe", TrainingType.YOGA);
        assertThat(updated).isEqualTo(expected);
        verify(credentialGenerator, times(1))
                .generateUsername(eq("John"), eq("Doe"), any(ConcurrentHashMap.class));
    }

    @Test
    void shouldThrowWhenUpdatingMissingTrainer() {
        when(trainerRepository.findById(99L)).thenReturn(Optional.empty());

        UpdateTrainerRequest request = new UpdateTrainerRequest(
                99L, new UserInfo("John", "Smith"), TrainingType.YOGA, true);

        assertThatThrownBy(() -> trainerService.update(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");

        verify(trainerRepository, never()).save(any());
    }

    @Test
    void shouldGetTrainerById() {
        TrainerEntity trainer = TestDataFactory.createTrainerWithCredentials();
        trainer.setUserId(1L);
        when(trainerRepository.findById(1L)).thenReturn(Optional.of(trainer));

        TrainerResponse response = trainerService.getById(1L);

        TrainerResponse expected = new TrainerResponse(1L, "John Smith", "John.Smith", TrainingType.YOGA);
        assertThat(response).isEqualTo(expected);
    }

    @Test
    void shouldFindAllTrainers() {
        TrainerEntity trainer = TestDataFactory.createTrainerWithCredentials();
        trainer.setUserId(1L);
        when(trainerRepository.findAll()).thenAnswer(inv -> Stream.of(trainer));

        List<TrainerResponse> all = trainerService.findAll();

        TrainerResponse expected = new TrainerResponse(1L, "John Smith", "John.Smith", TrainingType.YOGA);
        assertThat(all).containsExactly(expected);
    }

    @Test
    void shouldThrowWhenGettingMissingTrainer() {
        when(trainerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainerService.getById(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void shouldThrowWhenTrainerInactive() {
        TrainerEntity trainer = TestDataFactory.createTrainerWithCredentials();
        trainer.setUserId(2L);
        trainer.setActive(false);
        when(trainerRepository.findById(2L)).thenReturn(Optional.of(trainer));

        assertThatThrownBy(() -> trainerService.getActiveById(2L))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("inactive");
    }

    @Test
    void shouldThrowWhenSpecializationDoesNotMatch() {
        TrainerEntity trainer = TestDataFactory.createTrainerWithCredentials();
        trainer.setUserId(2L);
        trainer.setSpecialization(TrainingType.BOXING);
        when(trainerRepository.findById(2L)).thenReturn(Optional.of(trainer));

        assertThatThrownBy(() -> trainerService.getActiveForSpecialization(2L, TrainingType.YOGA))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("specialization");
    }

    @Test
    void shouldReturnActiveTrainerMatchingSpecialization() {
        TrainerEntity trainer = TestDataFactory.createTrainerWithCredentials();
        trainer.setUserId(2L);
        when(trainerRepository.findById(2L)).thenReturn(Optional.of(trainer));

        TrainerResponse response = trainerService.getActiveForSpecialization(2L, TrainingType.YOGA);

        TrainerResponse expected = new TrainerResponse(2L, "John Smith", "John.Smith", TrainingType.YOGA);
        assertThat(response).isEqualTo(expected);
    }

    @Test
    void shouldFindActiveTrainerBySpecialization() {
        TrainerEntity trainer = TestDataFactory.createTrainerWithCredentials();
        trainer.setUserId(5L);
        when(trainerRepository.findAll()).thenAnswer(inv -> Stream.of(trainer));

        TrainerResponse response = trainerService.findActiveBySpecialization(TrainingType.YOGA);

        TrainerResponse expected = new TrainerResponse(5L, "John Smith", "John.Smith", TrainingType.YOGA);
        assertThat(response).isEqualTo(expected);
    }

    @Test
    void shouldThrowWhenNoActiveTrainerForSpecialization() {
        when(trainerRepository.findAll()).thenAnswer(inv -> Stream.empty());

        assertThatThrownBy(() -> trainerService.findActiveBySpecialization(TrainingType.YOGA))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
