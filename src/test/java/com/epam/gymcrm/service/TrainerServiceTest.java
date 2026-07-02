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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.same;
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

    @Mock
    private TrainerMapper trainerMapper;

    @InjectMocks
    private TrainerService trainerService;

    @Test
    void shouldCreateTrainer() {
        TrainerDto request = TestDataFactory.trainerDto();
        TrainerEntity toCreate = TestDataFactory.createDefaultTrainer();
        TrainerEntity created = TestDataFactory.trainerWithId(1L, "John.Smith");
        TrainerDto expected = TestDataFactory.trainerDtoWithCredentials(1L, "John.Smith");

        when(trainerMapper.toEntity(request)).thenReturn(toCreate);
        when(userInitializationUtil.createUser(same(toCreate), any(), eq("Trainer")))
                .thenReturn(created);
        when(trainerMapper.toDto(created)).thenReturn(expected);

        TrainerDto actual = trainerService.createTrainer(request);

        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
        verify(trainerMapper, times(1)).toEntity(request);

        ArgumentCaptor<TrainerEntity> entityCaptor = ArgumentCaptor.forClass(TrainerEntity.class);
        verify(userInitializationUtil, times(1))
                .createUser(entityCaptor.capture(), any(), eq("Trainer"));
        assertThat(entityCaptor.getValue()).isSameAs(toCreate);
        verify(trainerMapper, times(1)).toDto(created);
    }

    @Test
    void shouldUpdateTrainer() {
        TrainerDto request = TestDataFactory.trainerDtoWithCredentials(1L, "John.Smith");
        request.getSpecialization().setTypeName("BOXING");
        TrainerEntity existing = TestDataFactory.trainerWithId(1L, "John.Smith");
        TrainerEntity toSave = TestDataFactory.trainerWithId(1L, "John.Smith");
        toSave.setSpecialization(TestDataFactory.trainingType("BOXING"));
        toSave.getSpecialization().setId(1L);
        TrainerEntity updated = TestDataFactory.trainerWithId(1L, "John.Smith");
        updated.setSpecialization(TestDataFactory.trainingType("BOXING"));
        updated.getSpecialization().setId(1L);
        TrainerDto expected = TestDataFactory.trainerDtoWithCredentials(1L, "John.Smith");
        expected.getSpecialization().setTypeName("BOXING");

        when(trainerRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(trainerMapper.toEntity(request)).thenReturn(toSave);
        when(trainerRepository.save(same(toSave))).thenReturn(updated);
        when(trainerMapper.toDto(updated)).thenReturn(expected);

        TrainerDto actual = trainerService.updateTrainer(request);

        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
        verify(trainerRepository, times(1)).findById(1L);
        verify(trainerMapper, times(1)).toEntity(request);

        ArgumentCaptor<TrainerEntity> saveCaptor = ArgumentCaptor.forClass(TrainerEntity.class);
        verify(trainerRepository, times(1)).save(saveCaptor.capture());
        assertThat(saveCaptor.getValue()).isSameAs(toSave);
        verify(trainerMapper, times(1)).toDto(updated);
    }

    @Test
    void shouldGetTrainerById() {
        TrainerEntity trainer = TestDataFactory.trainerWithId(1L, "John.Smith");
        TrainerDto expected = TestDataFactory.trainerDtoWithCredentials(1L, "John.Smith");
        when(trainerRepository.findById(1L)).thenReturn(Optional.of(trainer));
        when(trainerMapper.toDto(trainer)).thenReturn(expected);

        TrainerDto actual = trainerService.getTrainer(1L);

        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
        verify(trainerRepository, times(1)).findById(1L);
        verify(trainerMapper, times(1)).toDto(trainer);
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
        TrainerDto expected = TestDataFactory.trainerDtoWithCredentials(2L, "John.Smith");
        when(trainerRepository.findById(2L)).thenReturn(Optional.of(trainer));
        when(trainerMapper.toDto(trainer)).thenReturn(expected);

        TrainerDto actual = trainerService.getActiveTrainerForSpecialization(2L, "YOGA");

        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
        verify(trainerRepository, times(1)).findById(2L);
        verify(trainerMapper, times(1)).toDto(trainer);
    }

    @Test
    void shouldFindActiveTrainerBySpecialization() {
        TrainerEntity trainer = TestDataFactory.trainerWithId(5L, "John.Smith");
        TrainerDto expected = TestDataFactory.trainerDtoWithCredentials(5L, "John.Smith");
        when(trainerRepository.findAll()).thenReturn(Stream.of(trainer));
        when(trainerMapper.toDto(trainer)).thenReturn(expected);

        TrainerDto actual = trainerService.findActiveBySpecialization("YOGA");

        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
        verify(trainerRepository, times(1)).findAll();
        verify(trainerMapper, times(1)).toDto(trainer);
    }

    @Test
    void shouldThrowWhenNoActiveTrainerForSpecialization() {
        when(trainerRepository.findAll()).thenReturn(Stream.empty());

        assertThatThrownBy(() -> trainerService.findActiveBySpecialization("YOGA"))
                .isInstanceOf(EntityNotFoundException.class);

        verify(trainerRepository, times(1)).findAll();
    }
}
