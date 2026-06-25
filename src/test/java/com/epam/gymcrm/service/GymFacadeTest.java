package com.epam.gymcrm.service;

import com.epam.gymcrm.dto.AutoScheduleTrainingRequest;
import com.epam.gymcrm.dto.ScheduleTrainingRequest;
import com.epam.gymcrm.dto.TrainingResponse;
import com.epam.gymcrm.exception.InvalidOperationException;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.Training;
import com.epam.gymcrm.model.TrainingType;
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
class GymFacadeTest {

    @Mock
    private TrainerService trainerService;

    @Mock
    private TraineeService traineeService;

    @Mock
    private TrainingService trainingService;

    private GymFacade gymFacade;

    @BeforeEach
    void setUp() {
        gymFacade = new GymFacade(trainerService, traineeService, trainingService);
    }

    @Test
    void shouldScheduleTrainingDelegatingValidationToServices() {
        Trainee trainee = TestDataFactory.createTraineeWithCredentials();
        trainee.setUserId(1L);
        Trainer trainer = TestDataFactory.createTrainerWithCredentials();
        trainer.setUserId(2L);

        Training savedTraining = TestDataFactory.createDefaultTraining(1L, 2L);
        savedTraining.setTrainingId(10L);

        when(traineeService.getActiveById(1L)).thenReturn(trainee);
        when(trainerService.getActiveForSpecialization(2L, "Yoga")).thenReturn(trainer);
        when(trainingService.create(any(Training.class))).thenReturn(savedTraining);

        ScheduleTrainingRequest request = new ScheduleTrainingRequest(
                1L, 2L, "Morning Yoga", new TrainingType("Yoga"),
                LocalDate.of(2024, 3, 1), Duration.ofMinutes(60));

        TrainingResponse response = gymFacade.scheduleTraining(request);

        assertThat(response.trainingId()).isEqualTo(10L);
        assertThat(response.traineeId()).isEqualTo(1L);
        assertThat(response.trainerId()).isEqualTo(2L);
        verify(trainingService).create(any(Training.class));
    }

    @Test
    void shouldAutoAssignTrainerFromService() {
        Trainee trainee = TestDataFactory.createTraineeWithCredentials();
        trainee.setUserId(1L);
        Trainer yogaTrainer = TestDataFactory.createTrainerWithCredentials();
        yogaTrainer.setUserId(5L);

        when(traineeService.getActiveById(1L)).thenReturn(trainee);
        when(trainerService.findActiveBySpecialization("Yoga")).thenReturn(yogaTrainer);
        when(trainingService.create(any(Training.class))).thenAnswer(invocation -> {
            Training saved = invocation.getArgument(0);
            saved.setTrainingId(100L);
            return saved;
        });

        AutoScheduleTrainingRequest request = new AutoScheduleTrainingRequest(
                1L, "Morning Yoga", new TrainingType("Yoga"),
                LocalDate.now(), Duration.ofMinutes(60));

        TrainingResponse response = gymFacade.autoScheduleTraining(request);

        assertThat(response.trainerId()).isEqualTo(5L);
        assertThat(response.trainingName()).isEqualTo("Morning Yoga");
    }

    @Test
    void shouldNotRemoveTraineeWhenTrainingsExist() {
        when(trainingService.existsByTraineeId(1L)).thenReturn(true);

        assertThatThrownBy(() -> gymFacade.removeTraineeProfile(1L))
                .isInstanceOf(InvalidOperationException.class);

        verify(traineeService, never()).delete(eq(1L));
    }

    @Test
    void shouldRemoveTraineeWhenNoTrainingsExist() {
        when(trainingService.existsByTraineeId(1L)).thenReturn(false);

        gymFacade.removeTraineeProfile(1L);

        verify(traineeService).delete(1L);
    }
}
