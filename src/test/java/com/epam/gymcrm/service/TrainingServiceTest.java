package com.epam.gymcrm.service;

import com.epam.gymcrm.dto.AutoScheduleTrainingRequest;
import com.epam.gymcrm.dto.ScheduleTrainingRequest;
import com.epam.gymcrm.entity.TrainingEntity;
import com.epam.gymcrm.entity.TrainingType;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.repository.TrainingRepository;
import com.epam.gymcrm.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Stream;

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
        TrainingEntity training = TestDataFactory.createDefaultTraining(1L, 2L);
        when(trainingRepository.findAll()).thenAnswer(inv -> Stream.empty());
        trainingService.initIdSequence();

        when(trainingRepository.save(any(TrainingEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TrainingEntity created = trainingService.create(training);

        assertThat(created.getTrainingId()).isEqualTo(1L);
        verify(trainingRepository).save(training);
    }

    @Test
    void shouldFindTrainingById() {
        TrainingEntity training = TestDataFactory.createDefaultTraining(1L, 2L);
        training.setTrainingId(10L);
        when(trainingRepository.findById(10L)).thenReturn(Optional.of(training));

        assertThat(trainingService.findById(10L)).contains(training);
    }

    @Test
    void shouldFindAllTrainings() {
        TrainingEntity training = TestDataFactory.createDefaultTraining(1L, 2L);
        when(trainingRepository.findAll()).thenAnswer(inv -> Stream.of(training));

        assertThat(trainingService.findAll()).containsExactly(training);
    }

    @Test
    void shouldDetectExistingTrainingsByTraineeId() {
        TrainingEntity training = TestDataFactory.createDefaultTraining(1L, 2L);
        when(trainingRepository.findAll()).thenAnswer(inv -> Stream.of(training));

        assertThat(trainingService.existsByTraineeId(1L)).isTrue();
        assertThat(trainingService.existsByTraineeId(99L)).isFalse();
    }

    @Test
    void shouldUpdateExistingTraining() {
        TrainingEntity training = TestDataFactory.createDefaultTraining(1L, 2L);
        training.setTrainingId(10L);
        when(trainingRepository.findById(10L)).thenReturn(Optional.of(training));
        when(trainingRepository.update(training)).thenReturn(training);

        TrainingEntity updated = trainingService.update(training);

        assertThat(updated).isSameAs(training);
        verify(trainingRepository).update(training);
    }

    @Test
    void shouldThrowWhenUpdatingMissingTraining() {
        TrainingEntity training = TestDataFactory.createDefaultTraining(1L, 2L);
        training.setTrainingId(99L);
        when(trainingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainingService.update(training))
                .isInstanceOf(EntityNotFoundException.class);

        verify(trainingRepository, never()).update(any());
    }

    @Test
    void shouldDeleteExistingTraining() {
        TrainingEntity training = TestDataFactory.createDefaultTraining(1L, 2L);
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

    @Test
    void shouldScheduleTrainingFromRequest() {
        when(trainingRepository.findAll()).thenAnswer(inv -> Stream.empty());
        trainingService.initIdSequence();
        when(trainingRepository.save(any(TrainingEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ScheduleTrainingRequest request = new ScheduleTrainingRequest(
                1L, 2L, "Morning Yoga", TrainingType.YOGA,
                LocalDate.of(2024, 3, 1), Duration.ofMinutes(60));

        TrainingEntity result = trainingService.schedule(request);

        assertThat(result.getTraineeId()).isEqualTo(1L);
        assertThat(result.getTrainerId()).isEqualTo(2L);
        assertThat(result.getTrainingName()).isEqualTo("Morning Yoga");
        assertThat(result.getTrainingId()).isEqualTo(1L);
    }

    @Test
    void shouldAutoScheduleTrainingFromRequest() {
        when(trainingRepository.findAll()).thenAnswer(inv -> Stream.empty());
        trainingService.initIdSequence();
        when(trainingRepository.save(any(TrainingEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AutoScheduleTrainingRequest request = new AutoScheduleTrainingRequest(
                1L, "Boxing Session", TrainingType.BOXING,
                LocalDate.of(2024, 3, 1), Duration.ofMinutes(45));

        TrainingEntity result = trainingService.autoSchedule(request, 5L);

        assertThat(result.getTraineeId()).isEqualTo(1L);
        assertThat(result.getTrainerId()).isEqualTo(5L);
        assertThat(result.getTrainingName()).isEqualTo("Boxing Session");
        assertThat(result.getTrainingType()).isEqualTo(TrainingType.BOXING);
    }
}
