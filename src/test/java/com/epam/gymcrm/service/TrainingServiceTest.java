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
import org.mockito.ArgumentCaptor;
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

        TrainingResponse expected = new TrainingResponse(
                10L, "Morning Yoga", TrainingType.YOGA,
                LocalDate.of(2024, 3, 1), Duration.ofMinutes(60), 1L, 2L);
        assertThat(response).isEqualTo(expected);
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

        TrainingResponse expected = new TrainingResponse(
                7L, "Morning Yoga", TrainingType.YOGA,
                LocalDate.of(2024, 3, 1), Duration.ofMinutes(60), 1L, 2L);
        assertThat(all).containsExactly(expected);
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
        when(trainingRepository.save(any(TrainingEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        ScheduleTrainingRequest request = new ScheduleTrainingRequest(
                1L, 2L, "Morning Yoga", TrainingType.YOGA,
                LocalDate.of(2024, 3, 1), Duration.ofMinutes(60));

        TrainingResponse result = trainingService.schedule(request);

        TrainingResponse expected = new TrainingResponse(
                1L, "Morning Yoga", TrainingType.YOGA,
                LocalDate.of(2024, 3, 1), Duration.ofMinutes(60), 1L, 2L);
        assertThat(result).isEqualTo(expected);

        ArgumentCaptor<TrainingEntity> captor = ArgumentCaptor.forClass(TrainingEntity.class);
        verify(trainingRepository, times(1)).save(captor.capture());
        assertThat(captor.getValue().getTrainingName()).isEqualTo("Morning Yoga");
        assertThat(captor.getValue().getTraineeId()).isEqualTo(1L);
        assertThat(captor.getValue().getTrainerId()).isEqualTo(2L);
        verify(traineeService, times(1)).getById(1L);
        verify(trainerService, times(1)).getById(2L);
    }

    @Test
    void shouldAutoScheduleTrainingFromRequest() {
        when(trainingRepository.findAll()).thenAnswer(inv -> Stream.empty());
        trainingService.initIdSequence();
        when(trainingRepository.save(any(TrainingEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        AutoScheduleTrainingRequest request = new AutoScheduleTrainingRequest(
                1L, "Boxing Session", TrainingType.BOXING,
                LocalDate.of(2024, 3, 1), Duration.ofMinutes(45));

        TrainingResponse result = trainingService.autoSchedule(request, 5L);

        TrainingResponse expected = new TrainingResponse(
                1L, "Boxing Session", TrainingType.BOXING,
                LocalDate.of(2024, 3, 1), Duration.ofMinutes(45), 1L, 5L);
        assertThat(result).isEqualTo(expected);

        ArgumentCaptor<TrainingEntity> captor = ArgumentCaptor.forClass(TrainingEntity.class);
        verify(trainingRepository, times(1)).save(captor.capture());
        assertThat(captor.getValue().getTrainerId()).isEqualTo(5L);
        assertThat(captor.getValue().getTraineeId()).isEqualTo(1L);
        verify(traineeService, times(1)).getById(1L);
        verify(trainerService, times(1)).getById(5L);
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
