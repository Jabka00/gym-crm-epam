package com.epam.gymcrm.service;

import com.epam.gymcrm.dto.TrainingDto;
import com.epam.gymcrm.entity.TraineeEntity;
import com.epam.gymcrm.entity.TrainerEntity;
import com.epam.gymcrm.entity.TrainingEntity;
import com.epam.gymcrm.entity.TrainingTypeEntity;
import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.exception.InvalidOperationException;
import com.epam.gymcrm.mapper.TrainingMapper;
import com.epam.gymcrm.repository.TraineeRepository;
import com.epam.gymcrm.repository.TrainerRepository;
import com.epam.gymcrm.repository.TrainingRepository;
import com.epam.gymcrm.repository.TrainingTypeRepository;
import com.epam.gymcrm.security.AuthenticationGuard;
import com.epam.gymcrm.security.Credentials;
import com.epam.gymcrm.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
    private AuthenticationGuard authenticationGuard;

    @InjectMocks
    private TrainingService trainingService;

    private Credentials auth;

    @BeforeEach
    void setUp() {
        auth = TestDataFactory.credentials();
        doNothing().when(authenticationGuard).ensureAuthenticated(any(Credentials.class));
    }

    @Test
    void shouldCreateTraining() {
        TrainingDto request = TestDataFactory.trainingDto(1L, 2L);
        TraineeEntity trainee = TestDataFactory.traineeWithId(1L, "Alice.Walker");
        TrainerEntity trainer = TestDataFactory.trainerWithId(2L, "John.Smith");
        TrainingTypeEntity trainingType = TestDataFactory.yogaTypeEntity();
        TrainingEntity toSave = TestDataFactory.createDefaultTraining(1L, 2L);
        TrainingEntity saved = TestDataFactory.trainingWithId(10L, 1L, 2L);
        saved.setTrainee(trainee);
        saved.setTrainer(trainer);
        saved.setTrainingType(trainingType);
        TrainingDto expected = TestDataFactory.trainingDto(1L, 2L);
        expected.setId(10L);
        expected.setTrainee(TestDataFactory.traineeDtoWithCredentials(1L, "Alice.Walker"));
        expected.setTrainer(TestDataFactory.trainerDtoWithCredentials(2L, "John.Smith"));

        when(trainingMapper.toEntity(request)).thenReturn(toSave);
        when(traineeRepository.findById(1L)).thenReturn(Optional.of(trainee));
        when(trainerRepository.findById(2L)).thenReturn(Optional.of(trainer));
        when(trainingTypeRepository.findById(1L)).thenReturn(Optional.of(trainingType));
        when(trainingRepository.save(same(toSave))).thenReturn(saved);
        when(trainingMapper.toDto(saved)).thenReturn(expected);

        TrainingDto actual = trainingService.createTraining(auth, request);

        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
        verify(trainingMapper, times(1)).toEntity(request);
        verify(traineeRepository, times(1)).findById(1L);
        verify(trainerRepository, times(1)).findById(2L);
        verify(trainingTypeRepository, times(1)).findById(1L);

        ArgumentCaptor<TrainingEntity> trainingCaptor = ArgumentCaptor.forClass(TrainingEntity.class);
        verify(trainingRepository, times(1)).save(trainingCaptor.capture());
        TrainingEntity expectedToSave = TestDataFactory.createDefaultTraining(1L, 2L);
        expectedToSave.setTrainee(trainee);
        expectedToSave.setTrainer(trainer);
        expectedToSave.setTrainingType(trainingType);
        assertThat(trainingCaptor.getValue()).usingRecursiveComparison()
                .ignoringFields("id", "trainee.trainers", "trainee.trainings", "trainer.trainees", "trainer.trainings")
                .isEqualTo(expectedToSave);
        verify(trainingMapper, times(1)).toDto(saved);
        verify(authenticationGuard, times(1)).ensureAuthenticated(auth);
    }

    @Test
    void shouldGetTrainingById() {
        TrainingEntity training = TestDataFactory.trainingWithId(10L, 1L, 2L);
        TrainingDto expected = TestDataFactory.trainingDto(1L, 2L);
        expected.setId(10L);
        when(trainingRepository.findById(10L)).thenReturn(Optional.of(training));
        when(trainingMapper.toDto(training)).thenReturn(expected);

        TrainingDto actual = trainingService.getTraining(10L);

        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
        verify(trainingRepository, times(1)).findById(10L);
        verify(trainingMapper, times(1)).toDto(training);
        verify(authenticationGuard, never()).ensureAuthenticated(any());
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
        TrainingDto request = TestDataFactory.trainingDto(1L, 2L);
        TrainingEntity mapped = new TrainingEntity();
        when(trainingMapper.toEntity(request)).thenReturn(mapped);
        when(traineeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainingService.createTraining(auth, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Trainee");

        verify(trainingMapper, times(1)).toEntity(request);
        verify(traineeRepository, times(1)).findById(1L);
        verify(trainerRepository, never()).findById(2L);
        verify(trainingTypeRepository, never()).findById(1L);
        verify(trainingRepository, never()).save(eq(mapped));
    }

    @Test
    void shouldThrowWhenCreatingTrainingForMissingTrainer() {
        TrainingDto request = TestDataFactory.trainingDto(1L, 2L);
        TrainingEntity mapped = new TrainingEntity();
        when(trainingMapper.toEntity(request)).thenReturn(mapped);
        when(traineeRepository.findById(1L)).thenReturn(Optional.of(TestDataFactory.traineeWithId(1L, "Alice.Walker")));
        when(trainerRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainingService.createTraining(auth, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Trainer");

        verify(trainingMapper, times(1)).toEntity(request);
        verify(traineeRepository, times(1)).findById(1L);
        verify(trainerRepository, times(1)).findById(2L);
        verify(trainingTypeRepository, never()).findById(1L);
        verify(trainingRepository, never()).save(eq(mapped));
    }

    @Test
    void shouldThrowWhenCreatingTrainingForInactiveTrainee() {
        TrainingDto request = TestDataFactory.trainingDto(1L, 2L);
        TrainingEntity mapped = new TrainingEntity();
        TraineeEntity inactiveTrainee = TestDataFactory.traineeWithId(1L, "Inactive.User");
        inactiveTrainee.setActive(false);
        when(trainingMapper.toEntity(request)).thenReturn(mapped);
        when(traineeRepository.findById(1L)).thenReturn(Optional.of(inactiveTrainee));

        assertThatThrownBy(() -> trainingService.createTraining(auth, request))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("inactive");

        verify(trainingMapper, times(1)).toEntity(request);
        verify(traineeRepository, times(1)).findById(1L);
        verify(trainerRepository, never()).findById(2L);
        verify(trainingRepository, never()).save(eq(mapped));
    }

    @Test
    void shouldThrowWhenCreatingTrainingForInactiveTrainer() {
        TrainingDto request = TestDataFactory.trainingDto(1L, 2L);
        TrainingEntity mapped = new TrainingEntity();
        TrainerEntity inactiveTrainer = TestDataFactory.trainerWithId(2L, "Inactive.Trainer");
        inactiveTrainer.setActive(false);
        when(trainingMapper.toEntity(request)).thenReturn(mapped);
        when(traineeRepository.findById(1L)).thenReturn(Optional.of(TestDataFactory.traineeWithId(1L, "Alice.Walker")));
        when(trainerRepository.findById(2L)).thenReturn(Optional.of(inactiveTrainer));

        assertThatThrownBy(() -> trainingService.createTraining(auth, request))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("inactive");

        verify(trainingMapper, times(1)).toEntity(request);
        verify(traineeRepository, times(1)).findById(1L);
        verify(trainerRepository, times(1)).findById(2L);
        verify(trainingRepository, never()).save(eq(mapped));
    }

    @Test
    void shouldRejectUnauthenticatedTrainingCreation() {
        TrainingDto request = TestDataFactory.trainingDto(1L, 2L);
        doThrow(new AuthenticationException("Invalid credentials for username: Alice.Walker"))
                .when(authenticationGuard)
                .ensureAuthenticated(auth);

        assertThatThrownBy(() -> trainingService.createTraining(auth, request))
                .isInstanceOf(AuthenticationException.class);

        verify(authenticationGuard, times(1)).ensureAuthenticated(auth);
        verify(trainingMapper, never()).toEntity(request);
    }

    @Test
    void shouldGetTraineeTrainings() {
        TrainingEntity training = TestDataFactory.trainingWithId(10L, 1L, 2L);
        TrainingDto trainingDto = TestDataFactory.trainingDto(1L, 2L);
        trainingDto.setId(10L);
        when(trainingRepository.findByTraineeUsernameAndCriteria(
                "Alice.Walker",
                LocalDate.of(2024, 3, 1),
                LocalDate.of(2024, 3, 31),
                null,
                null)).thenReturn(List.of(training));
        when(trainingMapper.toDto(training)).thenReturn(trainingDto);

        List<TrainingDto> actual = trainingService.getTraineeTrainings(
                auth,
                "Alice.Walker",
                LocalDate.of(2024, 3, 1),
                LocalDate.of(2024, 3, 31),
                null,
                null);

        assertThat(actual).containsExactly(trainingDto);
        verify(authenticationGuard, times(1)).ensureAuthenticated(auth);
        verify(trainingRepository, times(1)).findByTraineeUsernameAndCriteria(
                "Alice.Walker",
                LocalDate.of(2024, 3, 1),
                LocalDate.of(2024, 3, 31),
                null,
                null);
    }

    @Test
    void shouldGetTrainerTrainings() {
        TrainingEntity training = TestDataFactory.trainingWithId(10L, 1L, 2L);
        TrainingDto trainingDto = TestDataFactory.trainingDto(1L, 2L);
        trainingDto.setId(10L);
        when(trainingRepository.findByTrainerUsernameAndCriteria(
                "John.Smith",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31),
                null)).thenReturn(List.of(training));
        when(trainingMapper.toDto(training)).thenReturn(trainingDto);

        List<TrainingDto> actual = trainingService.getTrainerTrainings(
                auth,
                "John.Smith",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31),
                null);

        assertThat(actual).containsExactly(trainingDto);
        verify(authenticationGuard, times(1)).ensureAuthenticated(auth);
        verify(trainingRepository, times(1)).findByTrainerUsernameAndCriteria(
                "John.Smith",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31),
                null);
    }

    @Test
    void shouldRejectUnauthenticatedTraineeTrainingsLookup() {
        doThrow(new AuthenticationException("Invalid credentials for username: Alice.Walker"))
                .when(authenticationGuard)
                .ensureAuthenticated(auth);

        assertThatThrownBy(() -> trainingService.getTraineeTrainings(
                auth, "Alice.Walker", null, null, null, null))
                .isInstanceOf(AuthenticationException.class);

        verify(authenticationGuard, times(1)).ensureAuthenticated(auth);
        verify(trainingRepository, never()).findByTraineeUsernameAndCriteria(
                any(), any(), any(), any(), any());
    }

    @Test
    void shouldRejectUnauthenticatedTrainerTrainingsLookup() {
        doThrow(new AuthenticationException("Invalid credentials for username: John.Smith"))
                .when(authenticationGuard)
                .ensureAuthenticated(auth);

        assertThatThrownBy(() -> trainingService.getTrainerTrainings(
                auth, "John.Smith", null, null, null))
                .isInstanceOf(AuthenticationException.class);

        verify(authenticationGuard, times(1)).ensureAuthenticated(auth);
        verify(trainingRepository, never()).findByTrainerUsernameAndCriteria(
                any(), any(), any(), any());
    }
}
