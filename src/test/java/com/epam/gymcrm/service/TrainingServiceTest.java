package com.epam.gymcrm.service;

import com.epam.gymcrm.dto.TrainingDto;
import com.epam.gymcrm.entity.TraineeEntity;
import com.epam.gymcrm.entity.TrainerEntity;
import com.epam.gymcrm.entity.TrainingEntity;
import com.epam.gymcrm.entity.TrainingTypeEntity;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.exception.InvalidOperationException;
import com.epam.gymcrm.mapper.TrainingMapper;
import com.epam.gymcrm.repository.TraineeRepository;
import com.epam.gymcrm.repository.TrainerRepository;
import com.epam.gymcrm.repository.TrainingRepository;
import com.epam.gymcrm.repository.TrainingTypeRepository;
import com.epam.gymcrm.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    @InjectMocks
    private TrainingService trainingService;

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

        TrainingDto actual = trainingService.createTraining(request);

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
    }

    @Test
    void shouldDetectExistingTrainingsByTraineeId() {
        when(trainingRepository.existsByTraineeId(1L)).thenReturn(true);
        when(trainingRepository.existsByTraineeId(99L)).thenReturn(false);

        assertThat(trainingService.existsByTraineeId(1L)).isTrue();
        assertThat(trainingService.existsByTraineeId(99L)).isFalse();
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
        when(trainingMapper.toEntity(request)).thenReturn(new TrainingEntity());
        when(traineeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainingService.createTraining(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Trainee");

        verify(trainingMapper, times(1)).toEntity(request);
        verify(traineeRepository, times(1)).findById(1L);
        verify(trainerRepository, never()).findById(2L);
        verify(trainingTypeRepository, never()).findById(1L);
        verify(trainingRepository, never()).save(org.mockito.ArgumentMatchers.any(TrainingEntity.class));
    }

    @Test
    void shouldThrowWhenCreatingTrainingForMissingTrainer() {
        TrainingDto request = TestDataFactory.trainingDto(1L, 2L);
        when(trainingMapper.toEntity(request)).thenReturn(new TrainingEntity());
        when(traineeRepository.findById(1L)).thenReturn(Optional.of(TestDataFactory.traineeWithId(1L, "Alice.Walker")));
        when(trainerRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainingService.createTraining(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Trainer");

        verify(trainingMapper, times(1)).toEntity(request);
        verify(traineeRepository, times(1)).findById(1L);
        verify(trainerRepository, times(1)).findById(2L);
        verify(trainingTypeRepository, never()).findById(1L);
        verify(trainingRepository, never()).save(org.mockito.ArgumentMatchers.any(TrainingEntity.class));
    }

    @Test
    void shouldThrowWhenCreatingTrainingForInactiveTrainee() {
        TrainingDto request = TestDataFactory.trainingDto(1L, 2L);
        TraineeEntity inactiveTrainee = TestDataFactory.traineeWithId(1L, "Inactive.User");
        inactiveTrainee.setActive(false);
        when(trainingMapper.toEntity(request)).thenReturn(new TrainingEntity());
        when(traineeRepository.findById(1L)).thenReturn(Optional.of(inactiveTrainee));

        assertThatThrownBy(() -> trainingService.createTraining(request))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("inactive");

        verify(trainerRepository, never()).findById(2L);
        verify(trainingRepository, never()).save(org.mockito.ArgumentMatchers.any(TrainingEntity.class));
    }

    @Test
    void shouldThrowWhenCreatingTrainingForInactiveTrainer() {
        TrainingDto request = TestDataFactory.trainingDto(1L, 2L);
        TrainerEntity inactiveTrainer = TestDataFactory.trainerWithId(2L, "Inactive.Trainer");
        inactiveTrainer.setActive(false);
        when(trainingMapper.toEntity(request)).thenReturn(new TrainingEntity());
        when(traineeRepository.findById(1L)).thenReturn(Optional.of(TestDataFactory.traineeWithId(1L, "Alice.Walker")));
        when(trainerRepository.findById(2L)).thenReturn(Optional.of(inactiveTrainer));

        assertThatThrownBy(() -> trainingService.createTraining(request))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("inactive");

        verify(trainingRepository, never()).save(org.mockito.ArgumentMatchers.any(TrainingEntity.class));
    }
}
