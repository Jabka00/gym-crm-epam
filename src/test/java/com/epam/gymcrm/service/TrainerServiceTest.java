package com.epam.gymcrm.service;

import com.epam.gymcrm.dto.TrainerDto;
import com.epam.gymcrm.entity.TrainerEntity;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.exception.InvalidOperationException;
import com.epam.gymcrm.mapper.TrainerMapper;
import com.epam.gymcrm.repository.TrainerRepository;
import com.epam.gymcrm.support.TestDataFactory;
import com.epam.gymcrm.util.UserInitializationUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainerServiceTest {

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private UserInitializationUtil userInitializationUtil;

    @Mock
    private UserService userService;

    @Spy
    private TrainerMapper trainerMapper = Mappers.getMapper(TrainerMapper.class);

    @InjectMocks
    private TrainerService trainerService;

    @Test
    void shouldCreateTrainer() {
        TrainerDto request = TestDataFactory.trainerDto();
        TrainerEntity created = TestDataFactory.trainerWithId(1L, "John.Smith");
        TrainerDto expected = trainerMapper.toDto(created);

        when(userInitializationUtil.createUser(any(TrainerEntity.class), any(), eq("Trainer")))
                .thenReturn(created);

        TrainerDto actual = trainerService.createTrainer(request);

        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);

        ArgumentCaptor<TrainerEntity> entityCaptor = ArgumentCaptor.forClass(TrainerEntity.class);
        verify(userInitializationUtil, times(1))
                .createUser(entityCaptor.capture(), any(), eq("Trainer"));
        assertThat(entityCaptor.getValue())
                .usingRecursiveComparison()
                .ignoringFields("id", "username", "password", "active", "trainees", "trainings", "specialization")
                .isEqualTo(trainerMapper.toEntity(request));
    }

    @Test
    void shouldUpdateTrainer() {
        TrainerDto request = TestDataFactory.trainerDtoWithCredentials(1L, "John.Smith");
        request.getSpecialization().setTypeName("BOXING");
        TrainerEntity existing = TestDataFactory.trainerWithId(1L, "John.Smith");
        TrainerEntity updated = trainerMapper.toEntity(request);
        TrainerDto expected = trainerMapper.toDto(updated);

        when(trainerRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(trainerRepository.save(any(TrainerEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TrainerDto actual = trainerService.updateTrainer(request);

        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
        verify(trainerRepository, times(1)).findById(1L);

        ArgumentCaptor<TrainerEntity> saveCaptor = ArgumentCaptor.forClass(TrainerEntity.class);
        verify(trainerRepository, times(1)).save(saveCaptor.capture());
        assertThat(saveCaptor.getValue()).usingRecursiveComparison()
                .ignoringFields("trainees", "trainings")
                .isEqualTo(trainerMapper.toEntity(request));
    }

    @Test
    void shouldGetTrainerById() {
        TrainerEntity trainer = TestDataFactory.trainerWithId(1L, "John.Smith");
        TrainerDto expected = trainerMapper.toDto(trainer);
        when(trainerRepository.findById(1L)).thenReturn(Optional.of(trainer));

        TrainerDto actual = trainerService.getTrainer(1L);

        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
        verify(trainerRepository, times(1)).findById(1L);
    }

    @Test
    void shouldThrowWhenTrainerInactive() {
        TrainerEntity trainer = TestDataFactory.trainerWithId(2L, "Inactive.Trainer");
        trainer.setActive(false);
        when(trainerRepository.findById(2L)).thenReturn(Optional.of(trainer));

        assertThatThrownBy(() -> trainerService.getTrainer(2L))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("inactive");

        assertThatThrownBy(() -> trainerService.getActiveTrainer(2L))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("inactive");
    }

    @Test
    void shouldReturnOnlyActiveTrainers() {
        TrainerEntity active = TestDataFactory.trainerWithId(1L, "Active.Trainer");
        TrainerEntity inactive = TestDataFactory.trainerWithId(2L, "Inactive.Trainer");
        inactive.setActive(false);
        when(trainerRepository.findAll()).thenReturn(Stream.of(active, inactive));

        List<TrainerDto> actual = trainerService.getAllTrainers();

        assertThat(actual).hasSize(1);
        assertThat(actual.getFirst().getId()).isEqualTo(1L);
    }

    @Test
    void shouldThrowWhenGettingInactiveTrainerByUsername() {
        TrainerEntity trainer = TestDataFactory.trainerWithId(2L, "Inactive.Trainer");
        trainer.setActive(false);
        when(trainerRepository.findByUsername("Inactive.Trainer")).thenReturn(Optional.of(trainer));

        assertThatThrownBy(() -> trainerService.getTrainerByUsername("Inactive.Trainer"))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("inactive");
    }

    @Test
    void shouldThrowWhenInactiveTrainerForSpecialization() {
        TrainerEntity trainer = TestDataFactory.trainerWithId(2L, "Inactive.Trainer");
        trainer.setActive(false);
        when(trainerRepository.findById(2L)).thenReturn(Optional.of(trainer));

        assertThatThrownBy(() -> trainerService.getActiveTrainerForSpecialization(2L, "YOGA"))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("inactive");
    }

    @Test
    void shouldThrowWhenSpecializationDoesNotMatch() {
        TrainerEntity trainer = TestDataFactory.trainerWithId(2L, "John.Smith");
        trainer.setSpecialization(TestDataFactory.trainingType("BOXING"));
        trainer.getSpecialization().setId(3L);
        when(trainerRepository.findById(2L)).thenReturn(Optional.of(trainer));

        assertThatThrownBy(() -> trainerService.getActiveTrainerForSpecialization(2L, "YOGA"))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("specialization");
        verify(trainerMapper, never()).toDto(any(TrainerEntity.class));
    }

    @Test
    void shouldReturnActiveTrainerMatchingSpecialization() {
        TrainerEntity trainer = TestDataFactory.trainerWithId(2L, "John.Smith");
        TrainerDto expected = trainerMapper.toDto(trainer);
        when(trainerRepository.findById(2L)).thenReturn(Optional.of(trainer));

        TrainerDto actual = trainerService.getActiveTrainerForSpecialization(2L, "YOGA");

        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
        verify(trainerRepository, times(1)).findById(2L);
    }

    @Test
    void shouldFindActiveTrainerBySpecialization() {
        TrainerEntity active = TestDataFactory.trainerWithId(5L, "John.Smith");
        TrainerEntity inactive = TestDataFactory.trainerWithId(6L, "Inactive.Trainer");
        inactive.setActive(false);
        when(trainerRepository.findAll()).thenReturn(Stream.of(inactive, active));

        TrainerDto actual = trainerService.findActiveBySpecialization("YOGA");

        assertThat(actual.getId()).isEqualTo(5L);
        verify(trainerRepository, times(1)).findAll();
    }

    @Test
    void shouldThrowWhenNoActiveTrainerForSpecialization() {
        when(trainerRepository.findAll()).thenReturn(Stream.empty());

        assertThatThrownBy(() -> trainerService.findActiveBySpecialization("YOGA"))
                .isInstanceOf(EntityNotFoundException.class);

        verify(trainerRepository, times(1)).findAll();
    }
}
