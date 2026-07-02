package com.epam.gymcrm.service;

import com.epam.gymcrm.dto.TraineeDto;
import com.epam.gymcrm.entity.TraineeEntity;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.exception.InvalidOperationException;
import com.epam.gymcrm.mapper.TraineeMapper;
import com.epam.gymcrm.mapper.TrainerMapper;
import com.epam.gymcrm.repository.TraineeRepository;
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
class TraineeServiceTest {

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private UserInitializationUtil userInitializationUtil;

    @Mock
    private UserService userService;

    @Spy
    private TraineeMapper traineeMapper = Mappers.getMapper(TraineeMapper.class);

    @Mock
    private TrainerMapper trainerMapper;

    @InjectMocks
    private TraineeService traineeService;

    @Test
    void shouldCreateTrainee() {
        TraineeDto request = TestDataFactory.traineeDto();
        TraineeEntity created = TestDataFactory.traineeWithId(1L, "Jane.Doe");
        created.setFirstName("Jane");
        created.setLastName("Doe");
        created.setDateOfBirth(request.getDateOfBirth());
        created.setAddress(request.getAddress());
        TraineeDto expected = traineeMapper.toDto(created);

        when(userInitializationUtil.createUser(any(TraineeEntity.class), any(), eq("Trainee")))
                .thenReturn(created);

        TraineeDto actual = traineeService.createTrainee(request);

        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);

        ArgumentCaptor<TraineeEntity> entityCaptor = ArgumentCaptor.forClass(TraineeEntity.class);
        verify(userInitializationUtil, times(1))
                .createUser(entityCaptor.capture(), any(), eq("Trainee"));
        assertThat(entityCaptor.getValue())
                .usingRecursiveComparison()
                .ignoringFields("id", "username", "password", "active", "trainers", "trainings")
                .isEqualTo(traineeMapper.toEntity(request));
    }

    @Test
    void shouldUpdateTrainee() {
        TraineeDto request = TestDataFactory.traineeDtoWithCredentials(1L, "Alice.Walker");
        request.setAddress("Odesa");
        TraineeEntity existing = TestDataFactory.traineeWithId(1L, "Alice.Walker");
        TraineeEntity updated = traineeMapper.toEntity(request);
        TraineeDto expected = traineeMapper.toDto(updated);

        when(traineeRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(traineeRepository.save(org.mockito.ArgumentMatchers.any(TraineeEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TraineeDto actual = traineeService.updateTrainee(request);

        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
        verify(traineeRepository, times(1)).findById(1L);

        ArgumentCaptor<TraineeEntity> saveCaptor = ArgumentCaptor.forClass(TraineeEntity.class);
        verify(traineeRepository, times(1)).save(saveCaptor.capture());
        assertThat(saveCaptor.getValue()).usingRecursiveComparison()
                .ignoringFields("trainers", "trainings")
                .isEqualTo(traineeMapper.toEntity(request));
    }

    @Test
    void shouldThrowWhenUpdatingMissingTrainee() {
        TraineeDto request = TestDataFactory.traineeDtoWithCredentials(99L, "Missing.User");
        when(traineeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> traineeService.updateTrainee(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");

        verify(traineeRepository, never()).save(any(TraineeEntity.class));
    }

    @Test
    void shouldGetTraineeById() {
        TraineeEntity trainee = TestDataFactory.traineeWithId(1L, "Alice.Walker");
        TraineeDto expected = traineeMapper.toDto(trainee);
        when(traineeRepository.findById(1L)).thenReturn(Optional.of(trainee));

        TraineeDto actual = traineeService.getTrainee(1L);

        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
        verify(traineeRepository, times(1)).findById(1L);
    }

    @Test
    void shouldGetActiveTrainee() {
        TraineeEntity trainee = TestDataFactory.traineeWithId(1L, "Alice.Walker");
        TraineeDto expected = traineeMapper.toDto(trainee);
        when(traineeRepository.findById(1L)).thenReturn(Optional.of(trainee));

        TraineeDto actual = traineeService.getActiveTrainee(1L);

        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    void shouldThrowWhenTraineeInactive() {
        TraineeEntity trainee = TestDataFactory.traineeWithId(2L, "Inactive.User");
        trainee.setActive(false);
        when(traineeRepository.findById(2L)).thenReturn(Optional.of(trainee));

        assertThatThrownBy(() -> traineeService.getActiveTrainee(2L))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("inactive");

        assertThatThrownBy(() -> traineeService.getTrainee(2L))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("inactive");
    }

    @Test
    void shouldReturnOnlyActiveTrainees() {
        TraineeEntity active = TestDataFactory.traineeWithId(1L, "Active.User");
        TraineeEntity inactive = TestDataFactory.traineeWithId(2L, "Inactive.User");
        inactive.setActive(false);
        when(traineeRepository.findAll()).thenReturn(Stream.of(active, inactive));

        List<TraineeDto> actual = traineeService.getAllTrainees();

        assertThat(actual).hasSize(1);
        assertThat(actual.getFirst().getId()).isEqualTo(1L);
    }

    @Test
    void shouldThrowWhenGettingInactiveTraineeByUsername() {
        TraineeEntity trainee = TestDataFactory.traineeWithId(2L, "Inactive.User");
        trainee.setActive(false);
        when(traineeRepository.findByUsername("Inactive.User")).thenReturn(Optional.of(trainee));

        assertThatThrownBy(() -> traineeService.getTraineeByUsername("Inactive.User"))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("inactive");
    }

    @Test
    void shouldDeleteTrainee() {
        TraineeEntity trainee = TestDataFactory.traineeWithId(1L, "Alice.Walker");
        when(traineeRepository.findById(1L)).thenReturn(Optional.of(trainee));

        traineeService.deleteTrainee(1L);

        verify(traineeRepository, times(1)).findById(1L);
        verify(traineeRepository, times(1)).delete(1L);
    }
}
