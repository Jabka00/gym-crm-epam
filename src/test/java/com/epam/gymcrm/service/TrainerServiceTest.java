package com.epam.gymcrm.service;

import com.epam.gymcrm.entity.TrainerEntity;
import com.epam.gymcrm.entity.TrainingType;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.exception.InvalidOperationException;
import com.epam.gymcrm.repository.TrainerRepository;
import com.epam.gymcrm.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainerServiceTest {

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private CredentialGenerator credentialGenerator;

    @InjectMocks
    private TrainerService trainerService;

    @Test
    void shouldCreateTrainerWithGeneratedCredentials() {
        TrainerEntity trainer = TestDataFactory.createDefaultTrainer();
        when(trainerRepository.findAll()).thenAnswer(inv -> Stream.empty());
        trainerService.initIdSequence();

        when(credentialGenerator.generateUsername(eq("John"), eq("Smith"), any(ConcurrentHashMap.class)))
                .thenReturn("John.Smith");
        when(credentialGenerator.generatePassword()).thenReturn("abcdefghij");
        when(trainerRepository.save(any(TrainerEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TrainerEntity created = trainerService.create(trainer);

        assertThat(created.getUserId()).isEqualTo(1L);
        assertThat(created.getUsername()).isEqualTo("John.Smith");
        assertThat(created.getPassword()).isEqualTo("abcdefghij");
        assertThat(created.isActive()).isTrue();
        verify(trainerRepository).save(trainer);
    }

    @Test
    void shouldUpdateTrainer() {
        TrainerEntity trainer = TestDataFactory.createTrainerWithCredentials();
        trainer.setUserId(1L);
        when(trainerRepository.update(trainer)).thenReturn(trainer);

        TrainerEntity updated = trainerService.update(trainer);

        assertThat(updated).isSameAs(trainer);
        verify(trainerRepository).update(trainer);
    }

    @Test
    void shouldFindTrainerById() {
        TrainerEntity trainer = TestDataFactory.createTrainerWithCredentials();
        trainer.setUserId(1L);
        when(trainerRepository.findById(1L)).thenReturn(Optional.of(trainer));

        assertThat(trainerService.findById(1L)).contains(trainer);
    }

    @Test
    void shouldFindAllTrainers() {
        TrainerEntity trainer = TestDataFactory.createTrainerWithCredentials();
        when(trainerRepository.findAll()).thenAnswer(inv -> Stream.of(trainer));

        assertThat(trainerService.findAll()).containsExactly(trainer);
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

        assertThat(trainerService.getActiveForSpecialization(2L, TrainingType.YOGA)).isSameAs(trainer);
    }

    @Test
    void shouldFindActiveTrainerBySpecialization() {
        TrainerEntity trainer = TestDataFactory.createTrainerWithCredentials();
        trainer.setUserId(5L);
        when(trainerRepository.findAll()).thenAnswer(inv -> Stream.of(trainer));

        assertThat(trainerService.findActiveBySpecialization(TrainingType.YOGA)).isSameAs(trainer);
    }

    @Test
    void shouldThrowWhenNoActiveTrainerForSpecialization() {
        when(trainerRepository.findAll()).thenAnswer(inv -> Stream.empty());

        assertThatThrownBy(() -> trainerService.findActiveBySpecialization(TrainingType.YOGA))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
