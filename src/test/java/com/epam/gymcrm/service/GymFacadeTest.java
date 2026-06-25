package com.epam.gymcrm.service;

import com.epam.gymcrm.exception.EntityNotFoundException;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
    void shouldScheduleTrainingWhenParticipantsAreActiveAndSpecializationMatches() {
        Trainee trainee = TestDataFactory.createDefaultTrainee();
        trainee.setUserId(1L);
        trainee.setActive(true);

        Trainer trainer = TestDataFactory.createDefaultTrainer();
        trainer.setUserId(2L);
        trainer.setActive(true);

        Training training = TestDataFactory.createDefaultTraining(1L, 2L);

        when(traineeService.findById(1L)).thenReturn(Optional.of(trainee));
        when(trainerService.findById(2L)).thenReturn(Optional.of(trainer));
        when(trainingService.create(training)).thenReturn(training);

        Training scheduled = gymFacade.scheduleTraining(training);

        assertThat(scheduled).isEqualTo(training);
        verify(trainingService).create(training);
    }

    @Test
    void shouldThrowWhenTrainerSpecializationDoesNotMatch() {
        Trainee trainee = TestDataFactory.createDefaultTrainee();
        trainee.setUserId(1L);
        trainee.setActive(true);

        Trainer trainer = new Trainer();
        trainer.setUserId(2L);
        trainer.setFirstName("Mike");
        trainer.setLastName("Brown");
        trainer.setActive(true);
        trainer.setSpecialization(new TrainingType("Boxing"));

        Training training = TestDataFactory.createDefaultTraining(1L, 2L);

        when(traineeService.findById(1L)).thenReturn(Optional.of(trainee));
        when(trainerService.findById(2L)).thenReturn(Optional.of(trainer));

        assertThatThrownBy(() -> gymFacade.scheduleTraining(training))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("specialization");

        verify(trainingService, never()).create(any());
    }

    @Test
    void shouldAutoAssignActiveTrainerBySpecialization() {
        Trainee trainee = TestDataFactory.createDefaultTrainee();
        trainee.setUserId(1L);
        trainee.setActive(true);

        Trainer yogaTrainer = TestDataFactory.createDefaultTrainer();
        yogaTrainer.setUserId(5L);
        yogaTrainer.setActive(true);

        when(traineeService.findById(1L)).thenReturn(Optional.of(trainee));
        when(trainerService.findAll()).thenReturn(List.of(yogaTrainer));
        when(trainingService.create(any(Training.class))).thenAnswer(invocation -> {
            Training saved = invocation.getArgument(0);
            saved.setTrainingId(100L);
            return saved;
        });

        Training scheduled = gymFacade.scheduleTrainingWithAvailableTrainer(
                1L,
                "Morning Yoga",
                new TrainingType("Yoga"),
                LocalDate.now(),
                Duration.ofMinutes(60));

        assertThat(scheduled.getTrainerId()).isEqualTo(5L);
        assertThat(scheduled.getTrainingName()).isEqualTo("Morning Yoga");
    }

    @Test
    void shouldNotRemoveTraineeWhenTrainingsExist() {
        when(traineeService.findById(1L)).thenReturn(Optional.of(TestDataFactory.createDefaultTrainee()));
        when(trainingService.existsByTraineeId(1L)).thenReturn(true);

        assertThatThrownBy(() -> gymFacade.removeTraineeProfile(1L))
                .isInstanceOf(InvalidOperationException.class);

        verify(traineeService, never()).delete(1L);
    }

    @Test
    void shouldRemoveTraineeWhenNoTrainingsExist() {
        when(traineeService.findById(1L)).thenReturn(Optional.of(TestDataFactory.createDefaultTrainee()));
        when(trainingService.existsByTraineeId(1L)).thenReturn(false);

        gymFacade.removeTraineeProfile(1L);

        verify(traineeService).delete(1L);
    }

    @Test
    void shouldThrowWhenTraineeIsInactive() {
        Trainee trainee = TestDataFactory.createDefaultTrainee();
        trainee.setUserId(1L);
        trainee.setActive(false);

        Training training = TestDataFactory.createDefaultTraining(1L, 2L);

        when(traineeService.findById(1L)).thenReturn(Optional.of(trainee));

        assertThatThrownBy(() -> gymFacade.scheduleTraining(training))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("inactive");

        verify(trainingService, never()).create(any());
    }

    @Test
    void shouldThrowWhenTrainerIsInactive() {
        Trainee trainee = TestDataFactory.createDefaultTrainee();
        trainee.setUserId(1L);
        trainee.setActive(true);

        Trainer trainer = TestDataFactory.createDefaultTrainer();
        trainer.setUserId(2L);
        trainer.setActive(false);

        Training training = TestDataFactory.createDefaultTraining(1L, 2L);

        when(traineeService.findById(1L)).thenReturn(Optional.of(trainee));
        when(trainerService.findById(2L)).thenReturn(Optional.of(trainer));

        assertThatThrownBy(() -> gymFacade.scheduleTraining(training))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("inactive");

        verify(trainingService, never()).create(any());
    }

    @Test
    void shouldThrowWhenNoActiveTrainerFoundForType() {
        Trainee trainee = TestDataFactory.createDefaultTrainee();
        trainee.setUserId(1L);
        trainee.setActive(true);

        when(traineeService.findById(1L)).thenReturn(Optional.of(trainee));
        when(trainerService.findAll()).thenReturn(List.of());

        assertThatThrownBy(() -> gymFacade.scheduleTrainingWithAvailableTrainer(
                1L, "Swim", new TrainingType("Swimming"), LocalDate.now(), Duration.ofMinutes(30)))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
