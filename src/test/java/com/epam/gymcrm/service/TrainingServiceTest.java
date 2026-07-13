package com.epam.gymcrm.service;

import com.epam.gymcrm.service.PasswordGenerator;

import com.epam.gymcrm.model.TrainingType;

import com.epam.gymcrm.dto.request.ScheduleTrainingRequest;
import com.epam.gymcrm.dto.response.Training;
import com.epam.gymcrm.entity.TraineeEntity;
import com.epam.gymcrm.entity.TrainerEntity;
import com.epam.gymcrm.entity.TrainingEntity;
import com.epam.gymcrm.entity.TrainingTypeEntity;
import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.exception.ValidationException;
import com.epam.gymcrm.mapper.TrainerMapper;
import com.epam.gymcrm.mapper.TrainingMapper;
import com.epam.gymcrm.repository.TraineeRepository;
import com.epam.gymcrm.repository.TrainerRepository;
import com.epam.gymcrm.repository.TrainingRepository;
import com.epam.gymcrm.repository.TrainingTypeRepository;
import com.epam.gymcrm.dto.Credentials;
import com.epam.gymcrm.support.TestDataFactory;
import com.epam.gymcrm.util.DtoValidator;
import org.junit.jupiter.api.BeforeEach;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingServiceTest {

    @Mock
    private TrainingRepository trainingRepository;

    @Mock
    private TrainingTypeRepository trainingTypeRepository;

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private TrainingMapper trainingMapper;

    @Mock
    private AuthenticationService authenticationService;

    @Spy
    private DtoValidator dtoValidator = new DtoValidator();

    @InjectMocks
    private TrainingService trainingService;

    private Credentials auth;

    @BeforeEach
    void setUp() {
        auth = TestDataFactory.credentials();
    }

    @Test
    void shouldRejectCreateWithMissingTrainingName() {
        ScheduleTrainingRequest request = new ScheduleTrainingRequest(
                1L, 2L, null, TrainingType.YOGA, LocalDate.of(2024, 3, 1), Duration.ofMinutes(60));

        assertThatThrownBy(() -> trainingService.createTraining(auth, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Name is required");

        verify(trainingMapper, never()).toEntity(
                any(ScheduleTrainingRequest.class), any(), any(), any());
    }

    @Test
    void shouldRejectCreateWithoutTrainerId() {
        ScheduleTrainingRequest request = new ScheduleTrainingRequest(
                1L, null, "Morning Yoga", TrainingType.YOGA, LocalDate.of(2024, 3, 1), Duration.ofMinutes(60));

        assertThatThrownBy(() -> trainingService.createTraining(auth, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Trainer id is required");

        verify(trainingMapper, never()).toEntity(
                any(ScheduleTrainingRequest.class), any(), any(), any());
    }

    @Test
    void shouldRejectCreateWithoutTraineeId() {
        ScheduleTrainingRequest request = new ScheduleTrainingRequest(
                null, 2L, "Morning Yoga", TrainingType.YOGA, LocalDate.of(2024, 3, 1), Duration.ofMinutes(60));

        assertThatThrownBy(() -> trainingService.createTraining(auth, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Trainee id is required");

        verify(trainingMapper, never()).toEntity(
                any(ScheduleTrainingRequest.class), any(), any(), any());
    }

    @Test
    void shouldCreateTraining() {
        ScheduleTrainingRequest request = TestDataFactory.scheduleTrainingRequest(1L, 2L, TrainingType.YOGA);
        TraineeEntity trainee = TestDataFactory.traineeWithId(1L, "Alice.Walker");
        TrainerEntity trainer = TestDataFactory.trainerWithId(2L, "John.Smith");
        TrainingTypeEntity trainingType = TestDataFactory.yogaTypeEntity();
        TrainingEntity toSave = TestDataFactory.createDefaultTraining(1L, 2L);
        TrainingEntity saved = TestDataFactory.trainingWithId(10L, 1L, 2L);
        Training expected = TestDataFactory.trainingResponse(10L);

        when(traineeRepository.findById(1L)).thenReturn(Optional.of(trainee));
        when(trainerRepository.findById(2L)).thenReturn(Optional.of(trainer));
        when(trainingTypeRepository.findByTypeName(TrainingType.YOGA)).thenReturn(Optional.of(trainingType));
        when(trainingMapper.toEntity(request, trainee, trainer, trainingType)).thenReturn(toSave);
        when(trainingRepository.save(toSave)).thenReturn(saved);
        when(trainingMapper.toResponse(saved)).thenReturn(expected);

        Training actual = trainingService.createTraining(auth, request);

        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
        verify(traineeRepository, times(1)).findById(1L);
        verify(trainerRepository, times(1)).findById(2L);
        verify(trainingTypeRepository, times(1)).findByTypeName(TrainingType.YOGA);
        verify(trainingMapper, times(1)).toEntity(request, trainee, trainer, trainingType);
        verify(trainingRepository, times(1)).save(toSave);
        verify(trainingMapper, times(1)).toResponse(saved);
        verify(authenticationService, times(1)).requireAuthenticated(auth);
    }

    @Test
    void shouldGetTrainingById() {
        TrainingEntity training = TestDataFactory.trainingWithId(10L, 1L, 2L);
        Training expected = TestDataFactory.trainingResponse(10L);
        when(trainingRepository.findById(10L)).thenReturn(Optional.of(training));
        when(trainingMapper.toResponse(training)).thenReturn(expected);

        Training actual = trainingService.getTraining(10L);

        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
        verify(trainingRepository, times(1)).findById(10L);
        verify(trainingMapper, times(1)).toResponse(training);
        verifyNoInteractions(authenticationService);
    }

    @Test
    void shouldDetectExistingTrainingsByTraineeId() {
        when(trainingRepository.existsByTraineeId(1L)).thenReturn(true);
        when(trainingRepository.existsByTraineeId(99L)).thenReturn(false);

        assertThat(trainingService.existsByTraineeId(1L)).isTrue();
        assertThat(trainingService.existsByTraineeId(99L)).isFalse();

        verify(trainingRepository, times(1)).existsByTraineeId(1L);
        verify(trainingRepository, times(1)).existsByTraineeId(99L);
    }

    @Test
    void shouldDeleteTraining() {
        TrainingEntity training = TestDataFactory.trainingWithId(10L, 1L, 2L);
        when(trainingRepository.findById(10L)).thenReturn(Optional.of(training));

        trainingService.deleteTraining(10L);

        verify(trainingRepository, times(1)).findById(10L);
        verify(trainingRepository, times(1)).delete(10L);
    }

    @Test
    void shouldThrowWhenCreatingTrainingForMissingTrainee() {
        ScheduleTrainingRequest request = TestDataFactory.scheduleTrainingRequest(1L, 2L, TrainingType.YOGA);
        when(traineeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainingService.createTraining(auth, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Trainee");

        verify(traineeRepository, times(1)).findById(1L);
        verify(trainerRepository, never()).findById(2L);
        verify(trainingTypeRepository, never()).findByTypeName(TrainingType.YOGA);
        verify(trainingRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenCreatingTrainingForMissingTrainer() {
        ScheduleTrainingRequest request = TestDataFactory.scheduleTrainingRequest(1L, 2L, TrainingType.YOGA);
        when(traineeRepository.findById(1L)).thenReturn(Optional.of(TestDataFactory.traineeWithId(1L, "Alice.Walker")));
        when(trainerRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainingService.createTraining(auth, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Trainer");

        verify(traineeRepository, times(1)).findById(1L);
        verify(trainerRepository, times(1)).findById(2L);
        verify(trainingTypeRepository, never()).findByTypeName(TrainingType.YOGA);
        verify(trainingRepository, never()).save(any());
    }

    @Test
    void shouldRejectUnauthenticatedTrainingCreation() {
        ScheduleTrainingRequest request = TestDataFactory.scheduleTrainingRequest(1L, 2L, TrainingType.YOGA);
        doThrow(new AuthenticationException("Invalid credentials"))
                .when(authenticationService)
                .requireAuthenticated(auth);

        assertThatThrownBy(() -> trainingService.createTraining(auth, request))
                .isInstanceOf(AuthenticationException.class);

        verify(authenticationService, times(1)).requireAuthenticated(auth);
        verify(trainingMapper, never()).toEntity(
                any(ScheduleTrainingRequest.class), any(), any(), any());
    }

    @Test
    void shouldGetTraineeTrainings() {
        TrainingEntity training = TestDataFactory.trainingWithId(10L, 1L, 2L);
        Training trainingResponse = TestDataFactory.trainingResponse(10L);
        when(trainingRepository.findByTraineeUsernameAndCriteria(
                "Alice.Walker",
                LocalDate.of(2024, 3, 1),
                LocalDate.of(2024, 3, 31),
                "John.Smith",
                TrainingType.YOGA)).thenReturn(List.of(training));
        when(trainingMapper.toResponse(training)).thenReturn(trainingResponse);

        List<Training> actual = trainingService.getTraineeTrainings(
                auth,
                "Alice.Walker",
                LocalDate.of(2024, 3, 1),
                LocalDate.of(2024, 3, 31),
                "John.Smith",
                TrainingType.YOGA);

        assertThat(actual).containsExactly(trainingResponse);
        verify(authenticationService, times(1)).requireAuthenticated(auth);
        verify(trainingRepository, times(1)).findByTraineeUsernameAndCriteria(
                "Alice.Walker",
                LocalDate.of(2024, 3, 1),
                LocalDate.of(2024, 3, 31),
                "John.Smith",
                TrainingType.YOGA);
    }

    @Test
    void shouldGetTrainerTrainings() {
        TrainingEntity training = TestDataFactory.trainingWithId(10L, 1L, 2L);
        Training trainingResponse = TestDataFactory.trainingResponse(10L);
        when(trainingRepository.findByTrainerUsernameAndCriteria(
                "John.Smith",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31),
                null)).thenReturn(List.of(training));
        when(trainingMapper.toResponse(training)).thenReturn(trainingResponse);

        List<Training> actual = trainingService.getTrainerTrainings(
                auth,
                "John.Smith",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31),
                null);

        assertThat(actual).containsExactly(trainingResponse);
        verify(authenticationService, times(1)).requireAuthenticated(auth);
        verify(trainingRepository, times(1)).findByTrainerUsernameAndCriteria(
                "John.Smith",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31),
                null);
    }

    @Test
    void shouldRejectUnauthenticatedTraineeTrainingsLookup() {
        doThrow(new AuthenticationException("Invalid credentials"))
                .when(authenticationService)
                .requireAuthenticated(auth);

        assertThatThrownBy(() -> trainingService.getTraineeTrainings(
                auth, "Alice.Walker", null, null, null, null))
                .isInstanceOf(AuthenticationException.class);

        verify(authenticationService, times(1)).requireAuthenticated(auth);
        verify(trainingRepository, never()).findByTraineeUsernameAndCriteria(
                "Alice.Walker", null, null, null, null);
    }

    @Test
    void shouldRejectUnauthenticatedTrainerTrainingsLookup() {
        doThrow(new AuthenticationException("Invalid credentials"))
                .when(authenticationService)
                .requireAuthenticated(auth);

        assertThatThrownBy(() -> trainingService.getTrainerTrainings(
                auth, "John.Smith", null, null, null))
                .isInstanceOf(AuthenticationException.class);

        verify(authenticationService, times(1)).requireAuthenticated(auth);
        verify(trainingRepository, never()).findByTrainerUsernameAndCriteria(
                "John.Smith", null, null, null);
    }

    @Test
    void shouldThrowWhenTrainingTypeMissingOnCreate() {
        ScheduleTrainingRequest request = TestDataFactory.scheduleTrainingRequest(1L, 2L, TrainingType.PILATES);
        when(traineeRepository.findById(1L)).thenReturn(Optional.of(TestDataFactory.traineeWithId(1L, "Alice.Walker")));
        when(trainerRepository.findById(2L)).thenReturn(Optional.of(TestDataFactory.trainerWithId(2L, "John.Smith")));
        when(trainingTypeRepository.findByTypeName(TrainingType.PILATES)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainingService.createTraining(auth, request))
                .isInstanceOf(EntityNotFoundException.class);

        verify(trainingRepository, never()).save(any());
    }

    @Test
    void shouldRejectBlankTraineeUsernameOnTrainingsLookup() {
        assertThatThrownBy(() -> trainingService.getTraineeTrainings(auth, " ", null, null, null, null))
                .isInstanceOf(ValidationException.class);

        verify(trainingRepository, never()).findByTraineeUsernameAndCriteria(any(), any(), any(), any(), any());
    }

    @Test
    void shouldRejectBlankTrainerUsernameOnTrainingsLookup() {
        assertThatThrownBy(() -> trainingService.getTrainerTrainings(auth, " ", null, null, null))
                .isInstanceOf(ValidationException.class);

        verify(trainingRepository, never()).findByTrainerUsernameAndCriteria(any(), any(), any(), any());
    }

    @Test
    void shouldThrowWhenDeletingMissingTraining() {
        when(trainingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainingService.deleteTraining(99L))
                .isInstanceOf(EntityNotFoundException.class);

        verify(trainingRepository, never()).delete(99L);
    }

    @Test
    void shouldReturnAllTrainings() {
        TrainingEntity training = TestDataFactory.trainingWithId(10L, 1L, 2L);
        Training response = TestDataFactory.trainingResponse(10L);
        when(trainingRepository.findAll()).thenReturn(java.util.stream.Stream.of(training));
        when(trainingMapper.toResponse(training)).thenReturn(response);

        assertThat(trainingService.getAllTrainings()).containsExactly(response);
    }

    @Test
    void shouldRejectCreateWithDurationShorterThanOneMinute() {
        ScheduleTrainingRequest request = new ScheduleTrainingRequest(
                1L, 2L, "Morning Yoga", TrainingType.YOGA, LocalDate.of(2024, 3, 1), Duration.ofSeconds(30));

        assertThatThrownBy(() -> trainingService.createTraining(auth, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Duration must be at least 1 minute");
    }

    @Test
    void trainingMapperShouldMapScheduleRequestToEntity() {
        TrainerMapper trainerMapper = new TrainerMapper(org.mockito.Mockito.mock(UserCredentialService.class), org.mockito.Mockito.mock(PasswordGenerator.class));
        TrainingMapper mapper = new TrainingMapper(trainerMapper);
        ScheduleTrainingRequest request = TestDataFactory.scheduleTrainingRequest(4L, 1L, TrainingType.YOGA);
        TraineeEntity trainee = TestDataFactory.traineeWithId(4L, "Alice.Walker");
        TrainerEntity trainer = TestDataFactory.trainerWithId(1L, "John.Smith");
        TrainingTypeEntity type = TestDataFactory.yogaTypeEntity();

        TrainingEntity actual = mapper.toEntity(request, trainee, trainer, type);

        assertThat(actual.getTrainee()).isEqualTo(trainee);
        assertThat(actual.getTrainer()).isEqualTo(trainer);
        assertThat(actual.getTrainingName()).isEqualTo("Morning Yoga");
        assertThat(actual.getTrainingType()).isEqualTo(type);
        assertThat(actual.getTrainingDate()).isEqualTo(LocalDate.of(2024, 3, 1));
        assertThat(actual.getDurationMinutes()).isEqualTo(60);
    }

    @Test
    void trainingMapperShouldMapEntityToResponse() {
        TrainerMapper trainerMapper = new TrainerMapper(org.mockito.Mockito.mock(UserCredentialService.class), org.mockito.Mockito.mock(PasswordGenerator.class));
        TrainingMapper mapper = new TrainingMapper(trainerMapper);
        TrainingEntity entity = TestDataFactory.trainingWithSeedAssociations(10L);

        Training actual = mapper.toResponse(entity);

        assertThat(actual.id()).isEqualTo(10L);
        assertThat(actual.name()).isEqualTo("Morning Yoga");
        assertThat(actual.type().typeName()).isEqualTo(TrainingType.YOGA);
        assertThat(actual.traineeId()).isEqualTo(4L);
        assertThat(actual.trainerId()).isEqualTo(1L);
    }
}
