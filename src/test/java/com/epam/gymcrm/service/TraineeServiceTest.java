package com.epam.gymcrm.service;

import com.epam.gymcrm.service.PasswordGenerator;

import com.epam.gymcrm.dto.request.CreateTraineeRequest;
import com.epam.gymcrm.dto.request.UpdateTraineeRequest;
import com.epam.gymcrm.dto.request.UserInfo;
import com.epam.gymcrm.dto.response.Trainee;
import com.epam.gymcrm.dto.response.Trainer;
import com.epam.gymcrm.entity.TraineeEntity;
import com.epam.gymcrm.entity.TrainerEntity;
import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.exception.InvalidOperationException;
import com.epam.gymcrm.exception.ValidationException;
import com.epam.gymcrm.mapper.TraineeMapper;
import com.epam.gymcrm.mapper.TrainerMapper;
import com.epam.gymcrm.repository.TraineeRepository;
import com.epam.gymcrm.repository.TrainerRepository;
import com.epam.gymcrm.dto.Credentials;
import com.epam.gymcrm.service.UserCredentialService;
import com.epam.gymcrm.support.TestDataFactory;
import com.epam.gymcrm.util.DtoValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TraineeServiceTest {

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private UserService userService;

    @Mock
    private AuthenticationService authenticationService;

    @Spy
    private DtoValidator dtoValidator = new DtoValidator();

    @Mock
    private TraineeMapper traineeMapper;

    @Mock
    private TrainerMapper trainerMapper;

    @InjectMocks
    private TraineeService traineeService;

    private Credentials auth;

    @BeforeEach
    void setUp() {
        auth = TestDataFactory.credentials();
    }

    @Test
    void shouldRejectCreateWithBlankFirstName() {
        CreateTraineeRequest request = TestDataFactory.createTraineeRequest("", "Doe");

        assertThatThrownBy(() -> traineeService.createTrainee(request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("First name is required");

        verifyNoInteractions(traineeMapper);
        verify(traineeRepository, never()).save(any());
    }

    @Test
    void shouldCreateTrainee() {
        CreateTraineeRequest request = TestDataFactory.createTraineeRequest();
        TraineeEntity mappedEntity = TestDataFactory.createDefaultTrainee();
        TraineeEntity created = TestDataFactory.traineeWithId(1L, "Jane.Doe");
        Trainee expected = new Trainee(
                1L,
                "Jane Doe",
                "Jane.Doe",
                LocalDate.of(1998, 5, 20),
                "Kyiv");

        when(traineeMapper.toEntity(request)).thenReturn(mappedEntity);
        when(traineeRepository.save(mappedEntity)).thenReturn(created);
        when(traineeMapper.toResponse(created)).thenReturn(expected);

        Trainee actual = traineeService.createTrainee(request);

        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
        verify(traineeMapper, times(1)).toEntity(request);
        verify(traineeRepository, times(1)).save(mappedEntity);
        verify(traineeMapper, times(1)).toResponse(created);
        verifyNoInteractions(authenticationService);
    }

    @Test
    void shouldUpdateTrainee() {
        UpdateTraineeRequest request = new UpdateTraineeRequest(
                1L,
                new UserInfo("Alice", "Walker"),
                true,
                LocalDate.of(1995, 4, 12),
                "Odesa");
        TraineeEntity existing = TestDataFactory.traineeWithId(1L, "Alice.Walker");
        TraineeEntity entityToSave = TestDataFactory.traineeWithId(1L, "Alice.Walker");
        entityToSave.setAddress("Odesa");
        Trainee expected = new Trainee(
                1L,
                "Alice Walker",
                "Alice.Walker",
                LocalDate.of(1995, 4, 12),
                "Odesa");

        when(traineeRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(traineeMapper.toEntity(request, existing.getUser().getUsername(), existing.getUser().getPassword()))
                .thenReturn(entityToSave);
        when(traineeRepository.save(entityToSave)).thenReturn(entityToSave);
        when(traineeMapper.toResponse(entityToSave)).thenReturn(expected);

        Trainee actual = traineeService.updateTrainee(auth, request);

        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
        verify(traineeRepository, times(1)).findById(1L);
        verify(traineeMapper, times(1))
                .toEntity(request, existing.getUser().getUsername(), existing.getUser().getPassword());
        verify(traineeRepository, times(1)).save(entityToSave);
        verify(authenticationService, times(1)).requireAuthenticated(auth);
    }

    @Test
    void shouldThrowWhenUpdatingMissingTrainee() {
        UpdateTraineeRequest request = TestDataFactory.updateTraineeRequest(99L);
        when(traineeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> traineeService.updateTrainee(auth, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");

        verify(traineeRepository, times(1)).findById(99L);
        verify(traineeRepository, never()).save(any());
    }

    @Test
    void shouldGetActiveTraineeById() {
        TraineeEntity trainee = TestDataFactory.traineeWithId(1L, "Alice.Walker");
        Trainee expected = TestDataFactory.traineeResponse(1L, "Alice.Walker");
        when(traineeRepository.findById(1L)).thenReturn(Optional.of(trainee));
        when(traineeMapper.toResponse(trainee)).thenReturn(expected);

        Trainee actual = traineeService.getActiveTrainee(1L);

        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
        verify(traineeRepository, times(1)).findById(1L);
    }

    @Test
    void shouldThrowWhenTraineeInactive() {
        TraineeEntity trainee = TestDataFactory.traineeWithId(2L, "Inactive.User");
        trainee.getUser().setActive(false);
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
        inactive.getUser().setActive(false);
        Trainee activeResponse = TestDataFactory.traineeResponse(1L, "Active.User");
        when(traineeRepository.findAll()).thenReturn(Stream.of(active, inactive));
        when(traineeMapper.toResponse(active)).thenReturn(activeResponse);

        List<Trainee> actual = traineeService.getAllTrainees();

        assertThat(actual).containsExactly(activeResponse);
        verify(traineeRepository, times(1)).findAll();
    }

    @Test
    void shouldThrowWhenGettingInactiveTraineeByUsername() {
        TraineeEntity trainee = TestDataFactory.traineeWithId(2L, "Inactive.User");
        trainee.getUser().setActive(false);
        when(traineeRepository.findByUsername("Inactive.User")).thenReturn(Optional.of(trainee));

        assertThatThrownBy(() -> traineeService.getTraineeByUsername(auth, "Inactive.User"))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("inactive");

        verify(traineeRepository, times(1)).findByUsername("Inactive.User");
        verify(authenticationService, times(1)).requireAuthenticated(auth);
    }

    @Test
    void shouldRejectUnauthenticatedTraineeLookup() {
        doThrow(new AuthenticationException("Invalid credentials"))
                .when(authenticationService)
                .requireAuthenticated(auth);

        assertThatThrownBy(() -> traineeService.getTraineeByUsername(auth, "Alice.Walker"))
                .isInstanceOf(AuthenticationException.class);

        verify(authenticationService, times(1)).requireAuthenticated(auth);
        verify(traineeRepository, never()).findByUsername("Alice.Walker");
    }

    @Test
    void shouldGetTraineeByUsernameWhenAuthenticated() {
        TraineeEntity trainee = TestDataFactory.traineeWithId(1L, "Alice.Walker");
        Trainee expected = TestDataFactory.traineeResponse(1L, "Alice.Walker");
        when(traineeRepository.findByUsername("Alice.Walker")).thenReturn(Optional.of(trainee));
        when(traineeMapper.toResponse(trainee)).thenReturn(expected);

        Trainee actual = traineeService.getTraineeByUsername(auth, "Alice.Walker");

        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
        verify(authenticationService, times(1)).requireAuthenticated(auth);
        verify(traineeRepository, times(1)).findByUsername("Alice.Walker");
    }

    @Test
    void shouldRejectUnauthenticatedUpdateTrainee() {
        UpdateTraineeRequest request = TestDataFactory.updateTraineeRequest(1L);
        doThrow(new AuthenticationException("Invalid credentials"))
                .when(authenticationService)
                .requireAuthenticated(auth);

        assertThatThrownBy(() -> traineeService.updateTrainee(auth, request))
                .isInstanceOf(AuthenticationException.class);

        verify(authenticationService, times(1)).requireAuthenticated(auth);
        verify(traineeRepository, never()).findById(1L);
        verify(traineeRepository, never()).save(any());
    }

    @Test
    void shouldRejectUnauthenticatedNotAssignedTrainersLookup() {
        doThrow(new AuthenticationException("Invalid credentials"))
                .when(authenticationService)
                .requireAuthenticated(auth);

        assertThatThrownBy(() -> traineeService.getNotAssignedTrainers(auth, "Alice.Walker"))
                .isInstanceOf(AuthenticationException.class);

        verify(authenticationService, times(1)).requireAuthenticated(auth);
        verify(trainerRepository, never()).findNotAssignedToTrainee("Alice.Walker");
    }

    @Test
    void shouldRejectUnauthenticatedToggleActivation() {
        doThrow(new AuthenticationException("Invalid credentials"))
                .when(authenticationService)
                .requireAuthenticated(auth);

        assertThatThrownBy(() -> traineeService.toggleActivation(auth, "Alice.Walker"))
                .isInstanceOf(AuthenticationException.class);

        verify(authenticationService, times(1)).requireAuthenticated(auth);
        verify(userService, never()).toggleActivation("Alice.Walker");
    }

    @Test
    void shouldDeleteTraineeByUsername() {
        TraineeEntity trainee = TestDataFactory.traineeWithId(1L, "Alice.Walker");
        when(traineeRepository.findByUsername("Alice.Walker")).thenReturn(Optional.of(trainee));

        traineeService.deleteTraineeByUsername(auth, "Alice.Walker");

        verify(authenticationService, times(1)).requireAuthenticated(auth);
        verify(traineeRepository, times(1)).findByUsername("Alice.Walker");
        verify(traineeRepository, times(1)).deleteByUsername("Alice.Walker");
    }

    @Test
    void shouldRejectUnauthenticatedTraineeDeletion() {
        doThrow(new AuthenticationException("Invalid credentials"))
                .when(authenticationService)
                .requireAuthenticated(auth);

        assertThatThrownBy(() -> traineeService.deleteTraineeByUsername(auth, "Alice.Walker"))
                .isInstanceOf(AuthenticationException.class);

        verify(authenticationService, times(1)).requireAuthenticated(auth);
        verify(traineeRepository, never()).deleteByUsername("Alice.Walker");
    }

    @Test
    void shouldGetNotAssignedTrainers() {
        TrainerEntity trainer = TestDataFactory.trainerWithId(3L, "Anna.Jones");
        Trainer trainerResponse = TestDataFactory.trainerResponse(3L, "Anna.Jones");
        when(trainerRepository.findNotAssignedToTrainee("Alice.Walker")).thenReturn(List.of(trainer));
        when(trainerMapper.toResponse(trainer)).thenReturn(trainerResponse);

        List<Trainer> actual = traineeService.getNotAssignedTrainers(auth, "Alice.Walker");

        assertThat(actual).containsExactly(trainerResponse);
        verify(authenticationService, times(1)).requireAuthenticated(auth);
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

        verify(authenticationService, times(1)).requireAuthenticated(auth);
        verify(traineeRepository, times(1)).save(trainee);
        assertThat(trainee.getTrainers()).containsExactly(trainer);
    }

    @Test
    void shouldRejectUnauthenticatedTrainersListUpdate() {
        doThrow(new AuthenticationException("Invalid credentials"))
                .when(authenticationService)
                .requireAuthenticated(auth);

        assertThatThrownBy(() -> traineeService.updateTrainersList(auth, "Alice.Walker", Set.of("John.Smith")))
                .isInstanceOf(AuthenticationException.class);

        verify(authenticationService, times(1)).requireAuthenticated(auth);
        verify(traineeRepository, never()).findByUsername("Alice.Walker");
    }

    @Test
    void shouldToggleActivation() {
        traineeService.toggleActivation(auth, "Alice.Walker");

        verify(authenticationService, times(1)).requireAuthenticated(auth);
        verify(userService, times(1)).toggleActivation("Alice.Walker");
    }

    @Test
    void shouldDelegateChangePasswordToUserService() {
        traineeService.changePassword(auth, "Alice.Walker", "oldPass1", "NewPass1!");

        verify(authenticationService, times(1)).requireAuthenticated(auth);
        verify(userService, times(1)).changePassword("Alice.Walker", "oldPass1", "NewPass1!");
    }

    @Test
    void shouldRejectUnauthenticatedChangePassword() {
        doThrow(new AuthenticationException("Invalid credentials"))
                .when(authenticationService)
                .requireAuthenticated(auth);

        assertThatThrownBy(() -> traineeService.changePassword(auth, "Alice.Walker", "oldPass1", "NewPass1!"))
                .isInstanceOf(AuthenticationException.class);

        verify(authenticationService, times(1)).requireAuthenticated(auth);
        verify(userService, never()).changePassword("Alice.Walker", "oldPass1", "NewPass1!");
    }

    @Test
    void shouldPropagateAuthenticationFailureFromUserServiceOnChangePassword() {
        doThrow(new AuthenticationException("Invalid credentials"))
                .when(userService)
                .changePassword("Alice.Walker", "wrong", "NewPass1!");

        assertThatThrownBy(() -> traineeService.changePassword(auth, "Alice.Walker", "wrong", "NewPass1!"))
                .isInstanceOf(AuthenticationException.class);

        verify(authenticationService, times(1)).requireAuthenticated(auth);
        verify(userService, times(1)).changePassword("Alice.Walker", "wrong", "NewPass1!");
    }

    @Test
    void shouldThrowWhenDeletingMissingTrainee() {
        when(traineeRepository.findByUsername("Missing.User")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> traineeService.deleteTraineeByUsername(auth, "Missing.User"))
                .isInstanceOf(EntityNotFoundException.class);

        verify(traineeRepository, never()).deleteByUsername("Missing.User");
    }

    @Test
    void shouldRejectBlankUsernameOnDelete() {
        assertThatThrownBy(() -> traineeService.deleteTraineeByUsername(auth, " "))
                .isInstanceOf(ValidationException.class);

        verify(traineeRepository, never()).findByUsername(any());
    }

    @Test
    void shouldThrowWhenUpdatingTrainersListForMissingTrainee() {
        when(traineeRepository.findByUsername("Missing.User")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> traineeService.updateTrainersList(auth, "Missing.User", Set.of("John.Smith")))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void shouldThrowWhenTrainerMissingInUpdateTrainersList() {
        TraineeEntity trainee = TestDataFactory.traineeWithId(1L, "Alice.Walker");
        when(traineeRepository.findByUsername("Alice.Walker")).thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUsernames(Set.of("Missing.Trainer"))).thenReturn(List.of());

        assertThatThrownBy(() -> traineeService.updateTrainersList(auth, "Alice.Walker", Set.of("Missing.Trainer")))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void shouldRejectInactiveTrainerInUpdateTrainersList() {
        TraineeEntity trainee = TestDataFactory.traineeWithId(1L, "Alice.Walker");
        TrainerEntity inactiveTrainer = TestDataFactory.trainerWithId(2L, "John.Smith");
        inactiveTrainer.getUser().setActive(false);
        when(traineeRepository.findByUsername("Alice.Walker")).thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUsernames(Set.of("John.Smith"))).thenReturn(List.of(inactiveTrainer));

        assertThatThrownBy(() -> traineeService.updateTrainersList(auth, "Alice.Walker", Set.of("John.Smith")))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("inactive");
    }

    @Test
    void shouldRejectNullTrainerSetInUpdateTrainersList() {
        assertThatThrownBy(() -> traineeService.updateTrainersList(auth, "Alice.Walker", null))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void shouldThrowWhenTraineeNotFoundByUsername() {
        when(traineeRepository.findByUsername("Missing.User")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> traineeService.getTraineeByUsername(auth, "Missing.User"))
                .isInstanceOf(EntityNotFoundException.class);

        verify(authenticationService, times(1)).requireAuthenticated(auth);
    }


    @Test
    void traineeMapperShouldMapEntityToResponse() {
        TraineeMapper mapper = new TraineeMapper(org.mockito.Mockito.mock(UserCredentialService.class), org.mockito.Mockito.mock(PasswordGenerator.class));
        TraineeEntity entity = TestDataFactory.traineeWithId(5L, "Jane.Doe");
        entity.getUser().setFirstName("Jane");
        entity.getUser().setLastName("Doe");
        entity.setDateOfBirth(LocalDate.of(1998, 5, 20));
        entity.setAddress("Kyiv");

        Trainee actual = mapper.toResponse(entity);

        assertThat(actual.userId()).isEqualTo(5L);
        assertThat(actual.fullName()).isEqualTo("Jane Doe");
        assertThat(actual.username()).isEqualTo("Jane.Doe");
        assertThat(actual.dateOfBirth()).isEqualTo(LocalDate.of(1998, 5, 20));
        assertThat(actual.address()).isEqualTo("Kyiv");
    }

    @Test
    void traineeMapperShouldMapCreateRequestToEntity() {
        UserCredentialService credentialService = org.mockito.Mockito.mock(UserCredentialService.class);
        when(credentialService.generateUniqueUsername("Jane", "Doe")).thenReturn("Jane.Doe");
        PasswordGenerator passwordGenerator = org.mockito.Mockito.mock(PasswordGenerator.class);
        when(passwordGenerator.generatePassword()).thenReturn("Pass1234");
        TraineeMapper mapper = new TraineeMapper(credentialService, passwordGenerator);
        CreateTraineeRequest request = TestDataFactory.createTraineeRequest("Jane", "Doe");

        TraineeEntity actual = mapper.toEntity(request);

        assertThat(actual.getUser().getFirstName()).isEqualTo("Jane");
        assertThat(actual.getUser().getLastName()).isEqualTo("Doe");
        assertThat(actual.getUser().getUsername()).isEqualTo("Jane.Doe");
        assertThat(actual.getUser().getPassword()).isEqualTo("Pass1234");
        assertThat(actual.getUser().isActive()).isTrue();
        assertThat(actual.getDateOfBirth()).isEqualTo(LocalDate.of(1998, 5, 20));
        assertThat(actual.getAddress()).isEqualTo("Kyiv");
    }

    @Test
    void traineeMapperShouldMapUpdateRequestToEntity() {
        TraineeMapper mapper = new TraineeMapper(org.mockito.Mockito.mock(UserCredentialService.class), org.mockito.Mockito.mock(PasswordGenerator.class));
        UpdateTraineeRequest request = new UpdateTraineeRequest(
                3L, new UserInfo("Jane", "Updated"), false, LocalDate.of(1990, 1, 1), "Lviv");

        TraineeEntity actual = mapper.toEntity(request, "Jane.Doe", "storedPass");

        assertThat(actual.getId()).isEqualTo(3L);
        assertThat(actual.getUser().getUsername()).isEqualTo("Jane.Doe");
        assertThat(actual.getUser().getPassword()).isEqualTo("storedPass");
        assertThat(actual.getUser().isActive()).isFalse();
        assertThat(actual.getAddress()).isEqualTo("Lviv");
    }
}
