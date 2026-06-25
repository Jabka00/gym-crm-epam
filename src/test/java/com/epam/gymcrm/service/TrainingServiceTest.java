package com.epam.gymcrm.service;

import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.model.Training;
import com.epam.gymcrm.repository.TrainingRepository;
import com.epam.gymcrm.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingServiceTest {

    @Mock
    private TrainingRepository trainingRepository;

    @InjectMocks
    private TrainingService trainingService;

    @Test
    void shouldCreateTraining() {
        Training training = TestDataFactory.createDefaultTraining(1L, 2L);
        when(trainingRepository.save(any(Training.class))).thenAnswer(invocation -> {
            Training saved = invocation.getArgument(0);
            saved.setTrainingId(10L);
            return saved;
        });

        Training created = trainingService.create(training);

        assertThat(created.getTrainingId()).isEqualTo(10L);
        verify(trainingRepository).save(training);
    }

    @Test
    void shouldFindTrainingById() {
        Training training = TestDataFactory.createDefaultTraining(1L, 2L);
        training.setTrainingId(10L);
        when(trainingRepository.findById(10L)).thenReturn(Optional.of(training));

        assertThat(trainingService.findById(10L)).contains(training);
    }

    @Test
    void shouldFindAllTrainings() {
        Training training = TestDataFactory.createDefaultTraining(1L, 2L);
        when(trainingRepository.findAll()).thenReturn(List.of(training));

        assertThat(trainingService.findAll()).containsExactly(training);
    }

    @Test
    void shouldDetectExistingTrainingsByTraineeId() {
        Training training = TestDataFactory.createDefaultTraining(1L, 2L);
        when(trainingRepository.findAll()).thenReturn(List.of(training));

        assertThat(trainingService.existsByTraineeId(1L)).isTrue();
        assertThat(trainingService.existsByTraineeId(99L)).isFalse();
    }

    @Test
    void shouldUpdateExistingTraining() {
        Training training = TestDataFactory.createDefaultTraining(1L, 2L);
        training.setTrainingId(10L);
        when(trainingRepository.findById(10L)).thenReturn(Optional.of(training));
        when(trainingRepository.update(training)).thenReturn(training);

        Training updated = trainingService.update(training);

        assertThat(updated).isSameAs(training);
        verify(trainingRepository).update(training);
    }

    @Test
    void shouldThrowWhenUpdatingMissingTraining() {
        Training training = TestDataFactory.createDefaultTraining(1L, 2L);
        training.setTrainingId(99L);
        when(trainingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainingService.update(training))
                .isInstanceOf(EntityNotFoundException.class);

        verify(trainingRepository, never()).update(any());
    }

    @Test
    void shouldDeleteExistingTraining() {
        Training training = TestDataFactory.createDefaultTraining(1L, 2L);
        training.setTrainingId(10L);
        when(trainingRepository.findById(10L)).thenReturn(Optional.of(training));

        trainingService.delete(10L);

        verify(trainingRepository).delete(10L);
    }

    @Test
    void shouldThrowWhenDeletingMissingTraining() {
        when(trainingRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainingService.delete(10L))
                .isInstanceOf(EntityNotFoundException.class);

        verify(trainingRepository, never()).delete(10L);
    }
}
