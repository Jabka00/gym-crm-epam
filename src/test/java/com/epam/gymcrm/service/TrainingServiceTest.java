package com.epam.gymcrm.service;

import com.epam.gymcrm.dto.AutoScheduleTrainingRequest;
import com.epam.gymcrm.dto.ScheduleTrainingRequest;
import com.epam.gymcrm.dto.TrainingResponse;
import com.epam.gymcrm.entity.TrainingEntity;
import com.epam.gymcrm.entity.TrainingType;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.mapper.TrainingMapper;
import com.epam.gymcrm.repository.TrainingRepository;
import com.epam.gymcrm.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingServiceTest {

    @Mock
    private TrainingRepository trainingRepository;

    @Mock
    private TraineeService traineeService;

    @Mock
    private TrainerService trainerService;

    @Spy
    private TrainingMapper trainingMapper = new TrainingMapper();

    @InjectMocks
    private TrainingService trainingService;

    @Test
    void shouldGetTrainingById() {
        TrainingEntity training = TestDataFactory.createDefaultTraining(1L, 2L);
        training.setTrainingId(10L);
        when(trainingRepository.findById(10L)).thenReturn(Optional.of(training));

        TrainingResponse response = trainingService.getById(10L);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.traineeId()).isEqualTo(1L);
        assertThat(response.trainerId()).isEqualTo(2L);
    }

    @Test
    void shouldThrowWhenGettingMissingTraining() {
        when(trainingRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainingService.getById(10L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void shouldFindAllTrainings() {
        TrainingEntity training = TestDataFactory.createDefaultTraining(1L, 2L);
        training.setTrainingId(7L);
        when(trainingRepository.findAll()).thenAnswer(inv -> Stream.of(training));

        List<TrainingResponse> all = trainingService.findAll();

        assertThat(all).extracting(TrainingResponse::id).containsExactly(7L);
    }

    @Test
    void shouldDetectExistingTrainingsByTraineeId() {
        TrainingEntity training = TestDataFactory.createDefaultTraining(1L, 2L);
        when(trainingRepository.findAll()).thenAnswer(inv -> Stream.of(training));

        assertThat(trainingService.existsByTraineeId(1L)).isTrue();
        assertThat(trainingService.existsByTraineeId(99L)).isFalse();
    }

    @Test
    void shouldDeleteExistingTraining() {
        TrainingEntity training = TestDataFactory.createDefaultTraining(1L, 2L);
        training.setTrainingId(10L);
        when(trainingRepository.findById(10L)).thenReturn(Optional.of(training));

        trainingService.delete(10L);

        verify(trainingRepository, times(1)).delete(10L);
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

        TrainingResponse result = trainingService.schedule(request);

        assertThat(result.traineeId()).isEqualTo(1L);
        assertThat(result.trainerId()).isEqualTo(2L);
        assertThat(result.name()).isEqualTo("Morning Yoga");
        assertThat(result.id()).isEqualTo(1L);
        verify(trainingRepository, times(1)).save(any(TrainingEntity.class));
    }

    @Test
    void shouldAutoScheduleTrainingFromRequest() {
        when(trainingRepository.findAll()).thenAnswer(inv -> Stream.empty());
        trainingService.initIdSequence();
        when(trainingRepository.save(any(TrainingEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AutoScheduleTrainingRequest request = new AutoScheduleTrainingRequest(
                1L, "Boxing Session", TrainingType.BOXING,
                LocalDate.of(2024, 3, 1), Duration.ofMinutes(45));

        TrainingResponse result = trainingService.autoSchedule(request, 5L);

        assertThat(result.traineeId()).isEqualTo(1L);
        assertThat(result.trainerId()).isEqualTo(5L);
        assertThat(result.name()).isEqualTo("Boxing Session");
        assertThat(result.type()).isEqualTo(TrainingType.BOXING);
    }

    @Test
    void shouldThrowWhenSchedulingWithUnknownTrainee() {
        when(traineeService.getById(1L))
                .thenThrow(new EntityNotFoundException("Trainee not found: id=1"));

        ScheduleTrainingRequest request = new ScheduleTrainingRequest(
                1L, 2L, "Morning Yoga", TrainingType.YOGA,
                LocalDate.of(2024, 3, 1), Duration.ofMinutes(60));

        assertThatThrownBy(() -> trainingService.schedule(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Trainee");

        verify(trainingRepository, never()).save(any(TrainingEntity.class));
    }

    @Test
    void shouldThrowWhenSchedulingWithUnknownTrainer() {
        when(trainerService.getById(2L))
                .thenThrow(new EntityNotFoundException("Trainer not found: id=2"));

        ScheduleTrainingRequest request = new ScheduleTrainingRequest(
                1L, 2L, "Morning Yoga", TrainingType.YOGA,
                LocalDate.of(2024, 3, 1), Duration.ofMinutes(60));

        assertThatThrownBy(() -> trainingService.schedule(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Trainer");

        verify(trainingRepository, never()).save(any(TrainingEntity.class));
    }
}
