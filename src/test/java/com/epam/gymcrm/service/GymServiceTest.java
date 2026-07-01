package com.epam.gymcrm.service;

import com.epam.gymcrm.dto.request.AutoScheduleTrainingRequest;
import com.epam.gymcrm.dto.request.ScheduleTrainingRequest;
import com.epam.gymcrm.dto.response.Trainer;
import com.epam.gymcrm.dto.response.Training;
import com.epam.gymcrm.model.TrainingType;
import com.epam.gymcrm.exception.InvalidOperationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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

    @InjectMocks
    private GymService gymService;

    @Test
    void shouldScheduleTrainingDelegatingValidationToServices() {
        ScheduleTrainingRequest request = new ScheduleTrainingRequest(
                1L, 2L, "Morning Yoga", TrainingType.YOGA,
                LocalDate.of(2024, 3, 1), Duration.ofMinutes(60));
        Training scheduled = new Training(
                10L, "Morning Yoga", TrainingType.YOGA,
                LocalDate.of(2024, 3, 1), Duration.ofMinutes(60), 1L, 2L);
        when(trainingService.schedule(request)).thenReturn(scheduled);

        Training response = gymService.scheduleTraining(request);

        assertThat(response).isEqualTo(scheduled);
        verify(traineeService, times(1)).getActiveById(1L);
        verify(trainerService, times(1)).getActiveForSpecialization(2L, TrainingType.YOGA);
        verify(trainingService, times(1)).schedule(request);
    }

    @Test
    void shouldAutoAssignTrainerFromService() {
        AutoScheduleTrainingRequest request = new AutoScheduleTrainingRequest(
                1L, "Morning Yoga", TrainingType.YOGA,
                LocalDate.of(2024, 3, 1), Duration.ofMinutes(60));
        Trainer trainer = new Trainer(5L, "John Smith", "John.Smith", TrainingType.YOGA);
        Training autoScheduled = new Training(
                100L, "Morning Yoga", TrainingType.YOGA,
                LocalDate.of(2024, 3, 1), Duration.ofMinutes(60), 1L, 5L);
        when(trainerService.findActiveBySpecialization(TrainingType.YOGA)).thenReturn(trainer);
        when(trainingService.autoSchedule(request, 5L)).thenReturn(autoScheduled);

        Training response = gymService.autoScheduleTraining(request);

        assertThat(response.trainerId()).isEqualTo(5L);
        assertThat(response.name()).isEqualTo("Morning Yoga");
        verify(traineeService, times(1)).getActiveById(1L);
        verify(trainingService, times(1)).autoSchedule(request, 5L);
    }

    @Test
    void shouldNotRemoveTraineeWhenTrainingsExist() {
        when(trainingService.existsByTraineeId(1L)).thenReturn(true);

        assertThatThrownBy(() -> gymService.removeTraineeProfile(1L))
                .isInstanceOf(InvalidOperationException.class);

        verify(traineeService, times(1)).getById(1L);
        verify(traineeService, never()).delete(eq(1L));
    }

    @Test
    void shouldRemoveTraineeWhenNoTrainingsExist() {
        when(trainingService.existsByTraineeId(1L)).thenReturn(false);

        gymService.removeTraineeProfile(1L);

        verify(traineeService, times(1)).getById(1L);
        verify(traineeService, times(1)).delete(1L);
    }
}
