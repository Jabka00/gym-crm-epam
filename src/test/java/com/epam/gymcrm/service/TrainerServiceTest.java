package com.epam.gymcrm.service;

import com.epam.gymcrm.dto.request.CreateTrainerRequest;
import com.epam.gymcrm.dto.request.UpdateTrainerRequest;
import com.epam.gymcrm.dto.request.UserInfo;
import com.epam.gymcrm.dto.response.Trainer;
import com.epam.gymcrm.entity.TrainerEntity;
import com.epam.gymcrm.entity.TrainingTypeEntity;
import com.epam.gymcrm.model.TrainingType;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.exception.InvalidOperationException;
import com.epam.gymcrm.mapper.TrainerMapper;
import com.epam.gymcrm.repository.TrainerRepository;
import com.epam.gymcrm.support.TestDataFactory;
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
    private UsernameGenerator usernameGenerator;

    @Mock
    private PasswordGenerator passwordGenerator;

    @Spy
    private TrainerMapper trainerMapper = Mappers.getMapper(TrainerMapper.class);

    @InjectMocks
    private TrainerService trainerService;

    @Test
    void shouldCreateTrainerWithGeneratedCredentials() {
        when(trainerRepository.findAll()).thenAnswer(inv -> Stream.empty());
        trainerService.initIdSequence();

        when(usernameGenerator.generateUsername(eq("John"), eq("Smith")))
                .thenReturn("John.Smith");
        when(passwordGenerator.generatePassword()).thenReturn("abcdefghij");
        when(trainerRepository.save(any(TrainerEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        CreateTrainerRequest request = new CreateTrainerRequest(
                new UserInfo("John", "Smith"), TrainingType.YOGA);

        Trainer created = trainerService.create(request);

        Trainer expected = new Trainer(1L, "John Smith", "John.Smith", TrainingType.YOGA);
        assertThat(created).isEqualTo(expected);

        ArgumentCaptor<TrainerEntity> captor = ArgumentCaptor.forClass(TrainerEntity.class);
        verify(trainerRepository, times(1)).save(captor.capture());
        assertThat(captor.getValue().getUsername()).isEqualTo("John.Smith");
        assertThat(captor.getValue().getPassword()).isEqualTo("abcdefghij");
        assertThat(captor.getValue().isActive()).isTrue();
        verify(passwordGenerator, times(1)).generatePassword();
    }

    @Test
    void shouldUpdateTrainerWithoutRegeneratingUsernameWhenNameUnchanged() {
        TrainerEntity existing = TestDataFactory.createTrainerWithCredentials();
        existing.setId(1L);
        when(trainerRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(trainerRepository.save(any(TrainerEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateTrainerRequest request = new UpdateTrainerRequest(
                1L, new UserInfo("John", "Smith"), TrainingType.BOXING, true);

        Trainer updated = trainerService.update(request);

        Trainer expected = new Trainer(1L, "John Smith", "John.Smith", TrainingType.BOXING);
        assertThat(updated).isEqualTo(expected);
        verify(usernameGenerator, never()).generateUsername(any(), any());
        verify(trainerRepository, times(1)).save(existing);
    }

    @Test
    void shouldRegenerateUsernameWhenNameChangesOnUpdate() {
        TrainerEntity existing = TestDataFactory.createTrainerWithCredentials();
        existing.setId(1L);
        when(trainerRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(trainerRepository.save(any(TrainerEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(usernameGenerator.generateUsername(eq("John"), eq("Doe")))
                .thenReturn("John.Doe");

        UpdateTrainerRequest request = new UpdateTrainerRequest(
                1L, new UserInfo("John", "Doe"), TrainingType.YOGA, true);

        Trainer updated = trainerService.update(request);

        Trainer expected = new Trainer(1L, "John Doe", "John.Doe", TrainingType.YOGA);
        assertThat(updated).isEqualTo(expected);
        verify(usernameGenerator, times(1))
                .generateUsername(eq("John"), eq("Doe"));
    }

    @Test
    void shouldThrowWhenUpdatingMissingTrainer() {
        when(trainerRepository.findById(99L)).thenReturn(Optional.empty());

        UpdateTrainerRequest request = new UpdateTrainerRequest(
                99L, new UserInfo("John", "Smith"), TrainingType.YOGA, true);

        assertThatThrownBy(() -> trainerService.update(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");

        verify(trainerRepository, never()).save(any());
    }

    @Test
    void shouldGetTrainerById() {
        TrainerEntity trainer = TestDataFactory.createTrainerWithCredentials();
        trainer.setId(1L);
        when(trainerRepository.findById(1L)).thenReturn(Optional.of(trainer));

        Trainer response = trainerService.getById(1L);

        Trainer expected = new Trainer(1L, "John Smith", "John.Smith", TrainingType.YOGA);
        assertThat(response).isEqualTo(expected);
    }

    @Test
    void shouldFindAllTrainers() {
        TrainerEntity trainer = TestDataFactory.createTrainerWithCredentials();
        trainer.setId(1L);
        when(trainerRepository.findAll()).thenAnswer(inv -> Stream.of(trainer));

        List<Trainer> all = trainerService.findAll();

        Trainer expected = new Trainer(1L, "John Smith", "John.Smith", TrainingType.YOGA);
        assertThat(all).containsExactly(expected);
    }

    @Test
    void shouldThrowWhenGettingMissingTrainer() {
        when(trainerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainerService.getById(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void shouldThrowWhenTrainerInactive() {
        TrainerEntity trainer = TestDataFactory.createTrainerWithCredentials();
        trainer.setId(2L);
        trainer.setActive(false);
        when(trainerRepository.findById(2L)).thenReturn(Optional.of(trainer));

        assertThatThrownBy(() -> trainerService.getActiveById(2L))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("inactive");
    }

    @Test
    void shouldThrowWhenSpecializationDoesNotMatch() {
        TrainerEntity trainer = TestDataFactory.createTrainerWithCredentials();
        trainer.setId(2L);
        trainer.setSpecialization(TrainingTypeEntity.of(TrainingType.BOXING));
        when(trainerRepository.findById(2L)).thenReturn(Optional.of(trainer));

        assertThatThrownBy(() -> trainerService.getActiveForSpecialization(2L, TrainingType.YOGA))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("specialization");
    }

    @Test
    void shouldReturnActiveTrainerMatchingSpecialization() {
        TrainerEntity trainer = TestDataFactory.createTrainerWithCredentials();
        trainer.setId(2L);
        when(trainerRepository.findById(2L)).thenReturn(Optional.of(trainer));

        Trainer response = trainerService.getActiveForSpecialization(2L, TrainingType.YOGA);

        Trainer expected = new Trainer(2L, "John Smith", "John.Smith", TrainingType.YOGA);
        assertThat(response).isEqualTo(expected);
    }

    @Test
    void shouldFindActiveTrainerBySpecialization() {
        TrainerEntity trainer = TestDataFactory.createTrainerWithCredentials();
        trainer.setId(5L);
        when(trainerRepository.findAll()).thenAnswer(inv -> Stream.of(trainer));

        Trainer response = trainerService.findActiveBySpecialization(TrainingType.YOGA);

        Trainer expected = new Trainer(5L, "John Smith", "John.Smith", TrainingType.YOGA);
        assertThat(response).isEqualTo(expected);
    }

    @Test
    void shouldThrowWhenNoActiveTrainerForSpecialization() {
        when(trainerRepository.findAll()).thenAnswer(inv -> Stream.empty());

        assertThatThrownBy(() -> trainerService.findActiveBySpecialization(TrainingType.YOGA))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
