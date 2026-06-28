package com.epam.gymcrm.service;

import com.epam.gymcrm.dto.AutoScheduleTrainingRequest;
import com.epam.gymcrm.dto.ScheduleTrainingRequest;
import com.epam.gymcrm.dto.TrainingResponse;
import com.epam.gymcrm.entity.TraineeEntity;
import com.epam.gymcrm.entity.TrainerEntity;
import com.epam.gymcrm.entity.TrainingEntity;
import com.epam.gymcrm.entity.TrainingType;
import com.epam.gymcrm.exception.InvalidOperationException;
import com.epam.gymcrm.mapper.TrainingMapper;
import com.epam.gymcrm.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GymServiceTest {

    @Mock
    private TrainerService trainerService;

    @Mock
    private TraineeService traineeService;

    @Mock
    private TrainingService trainingService;

    private GymService gymService;

    @BeforeEach
    void setUp() {
        gymService = new GymService(trainerService, traineeService, trainingService, new TrainingMapper());
    }

    @Test
    void shouldScheduleTrainingDelegatingValidationToServices() {
        TraineeEntity trainee = TestDataFactory.createTraineeWithCredentials();
        trainee.setUserId(1L);
        TrainerEntity trainer = TestDataFactory.createTrainerWithCredentials();
        trainer.setUserId(2L);

        TrainingEntity savedTraining = TestDataFactory.createDefaultTraining(1L, 2L);
        savedTraining.setTrainingId(10L);

        when(traineeService.getActiveById(1L)).thenReturn(trainee);
        when(trainerService.getActiveForSpecialization(2L, TrainingType.YOGA)).thenReturn(trainer);
        when(trainingService.schedule(any(ScheduleTrainingRequest.class))).thenReturn(savedTraining);

        ScheduleTrainingRequest request = new ScheduleTrainingRequest(
                1L, 2L, "Morning Yoga", TrainingType.YOGA,
                LocalDate.of(2024, 3, 1), Duration.ofMinutes(60));

        TrainingResponse response = gymService.scheduleTraining(request);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.traineeId()).isEqualTo(1L);
        assertThat(response.trainerId()).isEqualTo(2L);
        verify(trainingService).schedule(any(ScheduleTrainingRequest.class));
    }

    @Test
    void shouldAutoAssignTrainerFromService() {
        TraineeEntity trainee = TestDataFactory.createTraineeWithCredentials();
        trainee.setUserId(1L);
        TrainerEntity yogaTrainer = TestDataFactory.createTrainerWithCredentials();
        yogaTrainer.setUserId(5L);

        TrainingEntity savedTraining = TestDataFactory.createDefaultTraining(1L, 5L);
        savedTraining.setTrainingId(100L);

        when(traineeService.getActiveById(1L)).thenReturn(trainee);
        when(trainerService.findActiveBySpecialization(TrainingType.YOGA)).thenReturn(yogaTrainer);
        when(trainingService.autoSchedule(any(AutoScheduleTrainingRequest.class), eq(5L))).thenReturn(savedTraining);

        AutoScheduleTrainingRequest request = new AutoScheduleTrainingRequest(
                1L, "Morning Yoga", TrainingType.YOGA,
                LocalDate.now(), Duration.ofMinutes(60));

        TrainingResponse response = gymService.autoScheduleTraining(request);

        assertThat(response.trainerId()).isEqualTo(5L);
        assertThat(response.name()).isEqualTo("Morning Yoga");
    }

    @Test
    void shouldNotRemoveTraineeWhenTrainingsExist() {
        when(trainingService.existsByTraineeId(1L)).thenReturn(true);

        assertThatThrownBy(() -> gymService.removeTraineeProfile(1L))
                .isInstanceOf(InvalidOperationException.class);

        verify(traineeService, never()).delete(eq(1L));
    }

    @Test
    void shouldRemoveTraineeWhenNoTrainingsExist() {
        when(trainingService.existsByTraineeId(1L)).thenReturn(false);

        gymService.removeTraineeProfile(1L);

        verify(traineeService).delete(1L);
    }
}
