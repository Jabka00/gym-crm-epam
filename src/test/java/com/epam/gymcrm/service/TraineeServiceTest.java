package com.epam.gymcrm.service;

import com.epam.gymcrm.dto.TraineeDto;
import com.epam.gymcrm.dto.TrainerDto;
import com.epam.gymcrm.entity.TraineeEntity;
import com.epam.gymcrm.entity.TrainerEntity;
import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.exception.InvalidOperationException;
import com.epam.gymcrm.mapper.TraineeMapper;
import com.epam.gymcrm.mapper.TrainerMapper;
import com.epam.gymcrm.repository.TraineeRepository;
import com.epam.gymcrm.repository.TrainerRepository;
import com.epam.gymcrm.security.AuthenticationGuard;
import com.epam.gymcrm.security.Credentials;
import com.epam.gymcrm.support.TestDataFactory;
import com.epam.gymcrm.util.DtoValidator;
import com.epam.gymcrm.util.UserInitializationUtil;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
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

    @Mock
    private AuthenticationGuard authenticationGuard;

    @Spy
    private DtoValidator dtoValidator = new DtoValidator();

    @Spy
    private TraineeMapper traineeMapper = Mappers.getMapper(TraineeMapper.class);

    @Mock
    private TrainerMapper trainerMapper;

    @InjectMocks
    private TraineeService traineeService;

    private Credentials auth;

    @BeforeEach
    void setUp() {
        auth = TestDataFactory.credentials();
        doNothing().when(authenticationGuard).ensureAuthenticated(any(Credentials.class));
    }

    @Test
    void shouldRejectCreateWithBlankFirstName() {
        TraineeDto request = TraineeDto.builder().firstName("").lastName("Doe").build();

        assertThatThrownBy(() -> traineeService.createTrainee(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("First name is required");

        verify(userInitializationUtil, never()).createTrainee(any(), any());
    }

    @Test
    void shouldCreateTrainee() {
        TraineeDto request = TestDataFactory.traineeDto();
        TraineeEntity mappedEntity = traineeMapper.toEntity(request);
        TraineeEntity created = TestDataFactory.traineeWithId(1L, "Jane.Doe");
        created.setFirstName("Jane");
        created.setLastName("Doe");
        created.setDateOfBirth(request.getDateOfBirth());
        created.setAddress(request.getAddress());
        TraineeDto expected = traineeMapper.toDto(created);

        doAnswer(invocation -> {
            TraineeEntity entity = invocation.getArgument(0);
            UnaryOperator<TraineeEntity> saver = invocation.getArgument(1);
            when(traineeRepository.save(entity)).thenReturn(created);
            return saver.apply(entity);
        }).when(userInitializationUtil).createTrainee(
                argThat(entity -> matchesTraineeEntity(entity, mappedEntity)),
                argThat(TraineeServiceTest::isUnaryOperator));

        TraineeDto actual = traineeService.createTrainee(request);

        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);

        ArgumentCaptor<TraineeEntity> entityCaptor = ArgumentCaptor.forClass(TraineeEntity.class);
        verify(userInitializationUtil, times(1))
                .createTrainee(entityCaptor.capture(), argThat(TraineeServiceTest::isUnaryOperator));
        assertThat(entityCaptor.getValue())
                .usingRecursiveComparison()
                .ignoringFields("id", "username", "password", "active", "trainers", "trainings")
                .isEqualTo(mappedEntity);
        verify(traineeRepository, times(1)).save(entityCaptor.getValue());
        verify(authenticationGuard, never()).ensureAuthenticated(any());
    }

    @Test
    void shouldUpdateTrainee() {
        TraineeDto request = TestDataFactory.traineeDtoWithCredentials(1L, "Alice.Walker");
        request.setAddress("Odesa");
        TraineeEntity existing = TestDataFactory.traineeWithId(1L, "Alice.Walker");
        TraineeEntity entityToSave = traineeMapper.toEntity(request);
        TraineeDto expected = traineeMapper.toDto(entityToSave);

        when(traineeRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(traineeRepository.save(argThat(entity -> matchesTraineeEntity(entity, entityToSave))))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TraineeDto actual = traineeService.updateTrainee(auth, request);

        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
        verify(traineeRepository, times(1)).findById(1L);

        ArgumentCaptor<TraineeEntity> saveCaptor = ArgumentCaptor.forClass(TraineeEntity.class);
        verify(traineeRepository, times(1)).save(saveCaptor.capture());
        assertThat(saveCaptor.getValue()).usingRecursiveComparison()
                .ignoringFields("trainers", "trainings")
                .isEqualTo(entityToSave);
        verify(authenticationGuard, times(1)).ensureAuthenticated(auth);
    }

    @Test
    void shouldThrowWhenUpdatingMissingTrainee() {
        TraineeDto request = TestDataFactory.traineeDtoWithCredentials(99L, "Missing.User");
        TraineeEntity entityToSave = traineeMapper.toEntity(request);
        when(traineeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> traineeService.updateTrainee(auth, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");

        verify(traineeRepository, times(1)).findById(99L);
        verify(traineeRepository, never()).save(argThat(entity -> matchesTraineeEntity(entity, entityToSave)));
    }

    @Test
    void shouldGetActiveTraineeById() {
        TraineeEntity trainee = TestDataFactory.traineeWithId(1L, "Alice.Walker");
        TraineeDto expected = traineeMapper.toDto(trainee);
        when(traineeRepository.findById(1L)).thenReturn(Optional.of(trainee));

        TraineeDto actual = traineeService.getActiveTrainee(1L);

        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
        verify(traineeRepository, times(1)).findById(1L);
    }

    @Test
    void shouldThrowWhenTraineeInactive() {
        TraineeEntity trainee = TestDataFactory.traineeWithId(2L, "Inactive.User");
        trainee.setActive(false);
        when(traineeRepository.findById(2L)).thenReturn(Optional.of(trainee));

        assertThatThrownBy(() -> traineeService.getActiveTrainee(2L))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("inactive");

        verify(traineeRepository, times(1)).findById(2L);
    }

    @Test
    void shouldReturnOnlyActiveTrainees() {
        TraineeEntity active = TestDataFactory.traineeWithId(1L, "Active.User");
        TraineeEntity inactive = TestDataFactory.traineeWithId(2L, "Inactive.User");
        inactive.setActive(false);
        when(traineeRepository.findAll()).thenReturn(Stream.of(active, inactive));

        List<TraineeDto> actual = traineeService.getAllTrainees();
        List<TraineeDto> expected = List.of(traineeMapper.toDto(active));

        assertThat(actual)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyElementsOf(expected);
        verify(traineeRepository, times(1)).findAll();
    }

    @Test
    void shouldThrowWhenGettingInactiveTraineeByUsername() {
        TraineeEntity trainee = TestDataFactory.traineeWithId(2L, "Inactive.User");
        trainee.setActive(false);
        when(traineeRepository.findByUsername("Inactive.User")).thenReturn(Optional.of(trainee));

        assertThatThrownBy(() -> traineeService.getTraineeByUsername(auth, "Inactive.User"))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("inactive");

        verify(traineeRepository, times(1)).findByUsername("Inactive.User");
        verify(authenticationGuard, times(1)).ensureAuthenticated(auth);
    }

    @Test
    void shouldRejectUnauthenticatedTraineeLookup() {
        doThrow(new AuthenticationException("Invalid credentials for username: Alice.Walker"))
                .when(authenticationGuard)
                .ensureAuthenticated(auth);

        assertThatThrownBy(() -> traineeService.getTraineeByUsername(auth, "Alice.Walker"))
                .isInstanceOf(AuthenticationException.class);

        verify(authenticationGuard, times(1)).ensureAuthenticated(auth);
        verify(traineeRepository, never()).findByUsername(any());
    }

    @Test
    void shouldGetTraineeByUsernameWhenAuthenticated() {
        TraineeEntity trainee = TestDataFactory.traineeWithId(1L, "Alice.Walker");
        TraineeDto expected = traineeMapper.toDto(trainee);
        when(traineeRepository.findByUsername("Alice.Walker")).thenReturn(Optional.of(trainee));

        TraineeDto actual = traineeService.getTraineeByUsername(auth, "Alice.Walker");

        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
        verify(authenticationGuard, times(1)).ensureAuthenticated(auth);
        verify(traineeRepository, times(1)).findByUsername("Alice.Walker");
    }

    @Test
    void shouldRejectUnauthenticatedUpdateTrainee() {
        TraineeDto request = TestDataFactory.traineeDtoWithCredentials(1L, "Alice.Walker");
        doThrow(new AuthenticationException("Invalid credentials for username: Alice.Walker"))
                .when(authenticationGuard)
                .ensureAuthenticated(auth);

        assertThatThrownBy(() -> traineeService.updateTrainee(auth, request))
                .isInstanceOf(AuthenticationException.class);

        verify(authenticationGuard, times(1)).ensureAuthenticated(auth);
        verify(traineeRepository, never()).findById(any());
        verify(traineeRepository, never()).save(any());
    }

    @Test
    void shouldRejectUnauthenticatedNotAssignedTrainersLookup() {
        doThrow(new AuthenticationException("Invalid credentials for username: Alice.Walker"))
                .when(authenticationGuard)
                .ensureAuthenticated(auth);

        assertThatThrownBy(() -> traineeService.getNotAssignedTrainers(auth, "Alice.Walker"))
                .isInstanceOf(AuthenticationException.class);

        verify(authenticationGuard, times(1)).ensureAuthenticated(auth);
        verify(trainerRepository, never()).findNotAssignedToTrainee(any());
    }

    @Test
    void shouldRejectUnauthenticatedToggleActivation() {
        doThrow(new AuthenticationException("Invalid credentials for username: Alice.Walker"))
                .when(authenticationGuard)
                .ensureAuthenticated(auth);

        assertThatThrownBy(() -> traineeService.toggleActivation(auth, "Alice.Walker"))
                .isInstanceOf(AuthenticationException.class);

        verify(authenticationGuard, times(1)).ensureAuthenticated(auth);
        verify(userService, never()).toggleActivation(any());
    }

    @Test
    void shouldDeleteTraineeByUsername() {
        TraineeEntity trainee = TestDataFactory.traineeWithId(1L, "Alice.Walker");
        when(traineeRepository.findByUsername("Alice.Walker")).thenReturn(Optional.of(trainee));

        traineeService.deleteTraineeByUsername(auth, "Alice.Walker");

        verify(authenticationGuard, times(1)).ensureAuthenticated(auth);
        verify(traineeRepository, times(1)).findByUsername("Alice.Walker");
        verify(traineeRepository, times(1)).deleteByUsername("Alice.Walker");
    }

    @Test
    void shouldRejectUnauthenticatedTraineeDeletion() {
        doThrow(new AuthenticationException("Invalid credentials for username: Alice.Walker"))
                .when(authenticationGuard)
                .ensureAuthenticated(auth);

        assertThatThrownBy(() -> traineeService.deleteTraineeByUsername(auth, "Alice.Walker"))
                .isInstanceOf(AuthenticationException.class);

        verify(authenticationGuard, times(1)).ensureAuthenticated(auth);
        verify(traineeRepository, never()).deleteByUsername(any());
    }

    @Test
    void shouldGetNotAssignedTrainers() {
        TrainerEntity trainer = TestDataFactory.trainerWithId(3L, "Anna.Jones");
        TrainerDto trainerDto = TestDataFactory.trainerDtoWithCredentials(3L, "Anna.Jones");
        when(trainerRepository.findNotAssignedToTrainee("Alice.Walker")).thenReturn(List.of(trainer));
        when(trainerMapper.toDto(trainer)).thenReturn(trainerDto);

        List<TrainerDto> actual = traineeService.getNotAssignedTrainers(auth, "Alice.Walker");

        assertThat(actual).containsExactly(trainerDto);
        verify(authenticationGuard, times(1)).ensureAuthenticated(auth);
        verify(trainerRepository, times(1)).findNotAssignedToTrainee("Alice.Walker");
    }

    @Test
    void shouldUpdateTrainersList() {
        TraineeEntity trainee = TestDataFactory.traineeWithId(1L, "Alice.Walker");
        TrainerEntity trainer = TestDataFactory.trainerWithId(2L, "John.Smith");
        when(traineeRepository.findByUsername("Alice.Walker")).thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUsernames(Set.of("John.Smith"))).thenReturn(List.of(trainer));
        when(traineeRepository.save(trainee)).thenReturn(trainee);

        traineeService.updateTrainersList(auth, "Alice.Walker", Set.of("John.Smith"));

        verify(authenticationGuard, times(1)).ensureAuthenticated(auth);
        verify(traineeRepository, times(1)).save(trainee);
        assertThat(trainee.getTrainers()).containsExactly(trainer);
    }

    @Test
    void shouldRejectUnauthenticatedTrainersListUpdate() {
        doThrow(new AuthenticationException("Invalid credentials for username: Alice.Walker"))
                .when(authenticationGuard)
                .ensureAuthenticated(auth);

        assertThatThrownBy(() -> traineeService.updateTrainersList(auth, "Alice.Walker", Set.of("John.Smith")))
                .isInstanceOf(AuthenticationException.class);

        verify(authenticationGuard, times(1)).ensureAuthenticated(auth);
        verify(traineeRepository, never()).findByUsername(any());
    }

    @Test
    void shouldToggleActivation() {
        traineeService.toggleActivation(auth, "Alice.Walker");

        verify(authenticationGuard, times(1)).ensureAuthenticated(auth);
        verify(userService, times(1)).toggleActivation("Alice.Walker");
    }

    @Test
    void shouldDelegateChangePasswordToUserService() {
        traineeService.changePassword(auth, "Alice.Walker", "oldPass1", "NewPass1!");

        verify(authenticationGuard, times(1)).ensureAuthenticated(auth);
        verify(userService, times(1)).changePassword("Alice.Walker", "oldPass1", "NewPass1!");
    }

    @Test
    void shouldRejectUnauthenticatedChangePassword() {
        doThrow(new AuthenticationException("Invalid credentials for username: Alice.Walker"))
                .when(authenticationGuard)
                .ensureAuthenticated(auth);

        assertThatThrownBy(() -> traineeService.changePassword(auth, "Alice.Walker", "oldPass1", "NewPass1!"))
                .isInstanceOf(AuthenticationException.class);

        verify(authenticationGuard, times(1)).ensureAuthenticated(auth);
        verify(userService, never()).changePassword(any(), any(), any());
    }

    private static boolean matchesTraineeEntity(TraineeEntity actual, TraineeEntity expected) {
        try {
            assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
            return true;
        } catch (AssertionError error) {
            return false;
        }
    }

    private static boolean isUnaryOperator(Object value) {
        return value instanceof UnaryOperator<?>;
    }
}
