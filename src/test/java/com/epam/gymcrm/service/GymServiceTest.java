package com.epam.gymcrm.service;

import com.epam.gymcrm.dto.request.AutoScheduleTrainingRequest;
import com.epam.gymcrm.dto.request.ScheduleTrainingRequest;
import com.epam.gymcrm.dto.response.Trainee;
import com.epam.gymcrm.dto.response.Trainer;
import com.epam.gymcrm.dto.response.Training;
import com.epam.gymcrm.entity.TrainerEntity;
import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.exception.InvalidOperationException;
import com.epam.gymcrm.mapper.TrainerMapper;
import com.epam.gymcrm.repository.TrainerRepository;
import com.epam.gymcrm.security.Credentials;
import com.epam.gymcrm.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GymServiceTest {

    @Mock
    private TrainerService trainerService;

    @Mock
    private TraineeService traineeService;

    @Mock
    private TrainingService trainingService;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private TrainerMapper trainerMapper;

    @InjectMocks
    private GymService gymService;

    private Credentials auth;

    @BeforeEach
    void setUp() {
        auth = TestDataFactory.credentials();
    }

    @Test
    void shouldScheduleTraining() {
        ScheduleTrainingRequest request = TestDataFactory.scheduleTrainingRequest(1L, 2L, "YOGA");
        Trainee trainee = TestDataFactory.traineeResponse(1L, "Alice.Walker");
        Trainer trainer = TestDataFactory.trainerResponse(2L, "John.Smith");
        Training scheduled = TestDataFactory.trainingResponse(10L);

        when(traineeService.getActiveTrainee(1L)).thenReturn(trainee);
        when(trainerService.getActiveTrainerForSpecialization(2L, "YOGA")).thenReturn(trainer);
        when(trainingService.createTraining(auth, request)).thenReturn(scheduled);

        Training actual = gymService.scheduleTraining(auth, request);

        assertThat(actual).usingRecursiveComparison().isEqualTo(scheduled);
        verify(authenticationService, times(1)).requireAuthenticated(auth);
        verify(traineeService, times(1)).getActiveTrainee(1L);
        verify(trainerService, times(1)).getActiveTrainerForSpecialization(2L, "YOGA");
        verify(trainingService, times(1)).createTraining(auth, request);
    }

    @Test
    void shouldAutoScheduleTraining() {
        AutoScheduleTrainingRequest request = TestDataFactory.autoScheduleTrainingRequest(
                1L, "YOGA", LocalDate.of(2024, 3, 1));
        ScheduleTrainingRequest expectedScheduleRequest =
                TestDataFactory.scheduleTrainingRequest(1L, 5L, "YOGA", LocalDate.of(2024, 3, 1));
        Trainee trainee = TestDataFactory.traineeResponse(1L, "Alice.Walker");
        TrainerEntity trainerEntity = TestDataFactory.trainerWithId(5L, "John.Smith");
        Trainer trainer = TestDataFactory.trainerResponse(5L, "John.Smith");
        Training scheduled = TestDataFactory.trainingResponse(100L);

        when(traineeService.getActiveTrainee(1L)).thenReturn(trainee);
        when(trainerRepository.findActiveBySpecialization("YOGA")).thenReturn(Optional.of(trainerEntity));
        when(trainerMapper.toResponse(trainerEntity)).thenReturn(trainer);
        when(trainingService.createTraining(auth, expectedScheduleRequest)).thenReturn(scheduled);

        Training actual = gymService.autoScheduleTraining(auth, request);

        assertThat(actual).usingRecursiveComparison().isEqualTo(scheduled);
        verify(authenticationService, times(1)).requireAuthenticated(auth);
        verify(traineeService, times(1)).getActiveTrainee(1L);
        verify(trainerRepository, times(1)).findActiveBySpecialization("YOGA");
        verify(trainerMapper, times(1)).toResponse(trainerEntity);
        verify(trainingService, times(1)).createTraining(auth, expectedScheduleRequest);
    }

    @Test
    void shouldThrowWhenNoActiveTrainerForSpecializationDuringAutoSchedule() {
        AutoScheduleTrainingRequest request = TestDataFactory.autoScheduleTrainingRequest(1L, "YOGA");
        when(traineeService.getActiveTrainee(1L)).thenReturn(TestDataFactory.traineeResponse(1L, "Alice.Walker"));
        when(trainerRepository.findActiveBySpecialization("YOGA")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gymService.autoScheduleTraining(auth, request))
                .isInstanceOf(EntityNotFoundException.class);

        verify(authenticationService, times(1)).requireAuthenticated(auth);
        verify(traineeService, times(1)).getActiveTrainee(1L);
        verify(trainerRepository, times(1)).findActiveBySpecialization("YOGA");
        verify(trainerMapper, never()).toResponse(org.mockito.ArgumentMatchers.any());
        verifyNoInteractions(trainingService);
    }

    @Test
    void shouldPropagateAuthenticationFailureWhenSchedulingTraining() {
        ScheduleTrainingRequest request = TestDataFactory.scheduleTrainingRequest(1L, 2L, "YOGA");
        doThrow(new AuthenticationException("Invalid credentials for username: Alice.Walker"))
                .when(authenticationService)
                .requireAuthenticated(auth);

        assertThatThrownBy(() -> gymService.scheduleTraining(auth, request))
                .isInstanceOf(AuthenticationException.class);

        verify(authenticationService, times(1)).requireAuthenticated(auth);
        verifyNoInteractions(traineeService);
        verifyNoInteractions(trainerService);
        verifyNoInteractions(trainingService);
    }

    @Test
    void shouldRejectSchedulingWhenTraineeInactive() {
        ScheduleTrainingRequest request = TestDataFactory.scheduleTrainingRequest(1L, 2L, "YOGA");
        when(traineeService.getActiveTrainee(1L))
                .thenThrow(new InvalidOperationException("Trainee is inactive: id=1"));

        assertThatThrownBy(() -> gymService.scheduleTraining(auth, request))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("inactive");

        verify(authenticationService, times(1)).requireAuthenticated(auth);
        verify(traineeService, times(1)).getActiveTrainee(1L);
        verify(trainerService, never()).getActiveTrainerForSpecialization(2L, "YOGA");
        verifyNoInteractions(trainingService);
    }

    @Test
    void shouldRejectSchedulingWhenTrainerInactive() {
        ScheduleTrainingRequest request = TestDataFactory.scheduleTrainingRequest(1L, 2L, "YOGA");
        Trainee trainee = TestDataFactory.traineeResponse(1L, "Alice.Walker");
        when(traineeService.getActiveTrainee(1L)).thenReturn(trainee);
        when(trainerService.getActiveTrainerForSpecialization(2L, "YOGA"))
                .thenThrow(new InvalidOperationException("Trainer is inactive: id=2"));

        assertThatThrownBy(() -> gymService.scheduleTraining(auth, request))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("inactive");

        verify(authenticationService, times(1)).requireAuthenticated(auth);
        verify(traineeService, times(1)).getActiveTrainee(1L);
        verify(trainerService, times(1)).getActiveTrainerForSpecialization(2L, "YOGA");
        verifyNoInteractions(trainingService);
    }

    @Test
    void shouldRemoveTraineeProfile() {
        gymService.removeTraineeProfile(auth, "Alice.Walker");

        verify(traineeService, times(1)).deleteTraineeByUsername(auth, "Alice.Walker");
    }
}
