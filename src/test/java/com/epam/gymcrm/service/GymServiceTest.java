package com.epam.gymcrm.service;

import com.epam.gymcrm.dto.TraineeDto;
import com.epam.gymcrm.dto.TrainerDto;
import com.epam.gymcrm.dto.TrainingDto;
import com.epam.gymcrm.exception.InvalidOperationException;
import com.epam.gymcrm.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    void shouldScheduleTraining() {
        TrainingDto request = TestDataFactory.trainingDto(1L, 2L);
        TraineeDto trainee = TestDataFactory.traineeDtoWithCredentials(1L, "Alice.Walker");
        TrainerDto trainer = TestDataFactory.trainerDtoWithCredentials(2L, "John.Smith");
        TrainingDto enriched = TrainingDto.builder()
                .trainee(trainee)
                .trainer(trainer)
                .trainingName(request.getTrainingName())
                .trainingType(trainer.getSpecialization())
                .trainingDate(request.getTrainingDate())
                .trainingDuration(request.getTrainingDuration())
                .build();
        TrainingDto scheduled = TrainingDto.builder()
                .id(10L)
                .trainee(trainee)
                .trainer(trainer)
                .trainingName(request.getTrainingName())
                .trainingType(trainer.getSpecialization())
                .trainingDate(request.getTrainingDate())
                .trainingDuration(request.getTrainingDuration())
                .build();

        when(traineeService.getActiveTrainee(1L)).thenReturn(trainee);
        when(trainerService.getActiveTrainerForSpecialization(2L, "YOGA")).thenReturn(trainer);
        when(trainingService.createTraining(enriched)).thenReturn(scheduled);

        TrainingDto actual = gymService.scheduleTraining(request);

        assertThat(actual).usingRecursiveComparison().isEqualTo(scheduled);
        verify(traineeService, times(1)).getActiveTrainee(1L);
        verify(trainerService, times(1)).getActiveTrainerForSpecialization(2L, "YOGA");
        verify(trainingService, times(1)).createTraining(eq(enriched));
    }

    @Test
    void shouldAutoScheduleTraining() {
        TrainingDto request = TrainingDto.builder()
                .trainee(TraineeDto.builder().id(1L).build())
                .trainingName("Morning Yoga")
                .trainingType(TestDataFactory.yogaTypeDto())
                .trainingDate(LocalDate.of(2024, 3, 1))
                .trainingDuration(60)
                .build();
        TraineeDto trainee = TestDataFactory.traineeDtoWithCredentials(1L, "Alice.Walker");
        TrainerDto trainer = TestDataFactory.trainerDtoWithCredentials(5L, "John.Smith");
        TrainingDto enriched = TrainingDto.builder()
                .trainee(trainee)
                .trainer(trainer)
                .trainingName(request.getTrainingName())
                .trainingType(trainer.getSpecialization())
                .trainingDate(request.getTrainingDate())
                .trainingDuration(request.getTrainingDuration())
                .build();
        TrainingDto scheduled = TrainingDto.builder()
                .id(100L)
                .trainee(trainee)
                .trainer(trainer)
                .trainingName(request.getTrainingName())
                .trainingType(trainer.getSpecialization())
                .trainingDate(request.getTrainingDate())
                .trainingDuration(request.getTrainingDuration())
                .build();

        when(traineeService.getActiveTrainee(1L)).thenReturn(trainee);
        when(trainerService.findActiveBySpecialization("YOGA")).thenReturn(trainer);
        when(trainingService.createTraining(enriched)).thenReturn(scheduled);

        TrainingDto actual = gymService.autoScheduleTraining(request);

        assertThat(actual).usingRecursiveComparison().isEqualTo(scheduled);
        verify(traineeService, times(1)).getActiveTrainee(1L);
        verify(trainerService, times(1)).findActiveBySpecialization("YOGA");
        verify(trainingService, times(1)).createTraining(eq(enriched));
    }

    @Test
    void shouldRejectSchedulingWhenTraineeInactive() {
        TrainingDto request = TestDataFactory.trainingDto(1L, 2L);
        when(traineeService.getActiveTrainee(1L))
                .thenThrow(new InvalidOperationException("Trainee is inactive: id=1"));

        assertThatThrownBy(() -> gymService.scheduleTraining(request))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("inactive");

        verify(trainerService, never()).getActiveTrainerForSpecialization(2L, "YOGA");
        verify(trainingService, never()).createTraining(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldRejectSchedulingWhenTrainerInactive() {
        TrainingDto request = TestDataFactory.trainingDto(1L, 2L);
        TraineeDto trainee = TestDataFactory.traineeDtoWithCredentials(1L, "Alice.Walker");
        when(traineeService.getActiveTrainee(1L)).thenReturn(trainee);
        when(trainerService.getActiveTrainerForSpecialization(2L, "YOGA"))
                .thenThrow(new InvalidOperationException("Trainer is inactive: id=2"));

        assertThatThrownBy(() -> gymService.scheduleTraining(request))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("inactive");

        verify(trainingService, never()).createTraining(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldNotRemoveTraineeWhenTrainingsExist() {
        when(trainingService.existsByTraineeId(1L)).thenReturn(true);

        assertThatThrownBy(() -> gymService.removeTraineeProfile(1L))
                .isInstanceOf(InvalidOperationException.class);

        verify(traineeService, never()).deleteTrainee(1L);
    }

    @Test
    void shouldRemoveTraineeWhenNoTrainingsExist() {
        when(trainingService.existsByTraineeId(1L)).thenReturn(false);

        gymService.removeTraineeProfile(1L);

        verify(traineeService, times(1)).deleteTrainee(1L);
    }
}
