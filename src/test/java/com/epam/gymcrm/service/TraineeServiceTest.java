package com.epam.gymcrm.service;

import com.epam.gymcrm.service.PasswordGenerator;

import com.epam.gymcrm.dto.request.CreateTraineeRequest;
import com.epam.gymcrm.dto.request.UpdateTraineeRequest;
import com.epam.gymcrm.dto.request.UserInfo;
import com.epam.gymcrm.dto.Trainee;
import com.epam.gymcrm.dto.Trainer;
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
import com.epam.gymcrm.dto.request.ChangePasswordRequest;
import com.epam.gymcrm.dto.request.ToggleActivationRequest;
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

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
        lenient().when(authenticationService.matchesTraineeCredentials(auth.username(), auth.password()))
                .thenReturn(true);
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
        when(traineeMapper.toEntity(existing, request)).thenReturn(entityToSave);
        when(traineeRepository.save(entityToSave)).thenReturn(entityToSave);
        when(traineeMapper.toResponse(entityToSave)).thenReturn(expected);

        Trainee actual = traineeService.updateTrainee(auth, request);

        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
        verify(traineeRepository, times(1)).findById(1L);
        verify(traineeMapper, times(1)).toEntity(existing, request);
        verify(traineeRepository, times(1)).save(entityToSave);
        verify(authenticationService, times(1)).matchesTraineeCredentials(auth.username(), auth.password());
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
    void shouldThrowWhenGettingInactiveTraineeByUsername() {
        TraineeEntity trainee = TestDataFactory.traineeWithId(2L, "Inactive.User");
        trainee.getUser().setActive(false);
        when(traineeRepository.findByUsername("Inactive.User")).thenReturn(Optional.of(trainee));

        assertThatThrownBy(() -> traineeService.getTraineeByUsername(auth, "Inactive.User"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Trainee not found");

        verify(traineeRepository, times(1)).findByUsername("Inactive.User");
        verify(authenticationService, times(1)).matchesTraineeCredentials(auth.username(), auth.password());
    }

    @Test
    void shouldRejectUnauthenticatedTraineeLookup() {
        when(authenticationService.matchesTraineeCredentials(auth.username(), auth.password())).thenReturn(false);

        assertThatThrownBy(() -> traineeService.getTraineeByUsername(auth, "Alice.Walker"))
                .isInstanceOf(AuthenticationException.class);

        verify(authenticationService, times(1)).matchesTraineeCredentials(auth.username(), auth.password());
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
        verify(authenticationService, times(1)).matchesTraineeCredentials(auth.username(), auth.password());
        verify(traineeRepository, times(1)).findByUsername("Alice.Walker");
    }

    @Test
    void shouldRejectUnauthenticatedUpdateTrainee() {
        UpdateTraineeRequest request = TestDataFactory.updateTraineeRequest(1L);
        when(authenticationService.matchesTraineeCredentials(auth.username(), auth.password())).thenReturn(false);

        assertThatThrownBy(() -> traineeService.updateTrainee(auth, request))
                .isInstanceOf(AuthenticationException.class);

        verify(authenticationService, times(1)).matchesTraineeCredentials(auth.username(), auth.password());
        verify(traineeRepository, never()).findById(1L);
        verify(traineeRepository, never()).save(any());
    }

    @Test
    void shouldRejectUnauthenticatedNotAssignedTrainersLookup() {
        when(authenticationService.matchesTraineeCredentials(auth.username(), auth.password())).thenReturn(false);

        assertThatThrownBy(() -> traineeService.getNotAssignedTrainers(auth, "Alice.Walker"))
                .isInstanceOf(AuthenticationException.class);

        verify(authenticationService, times(1)).matchesTraineeCredentials(auth.username(), auth.password());
        verify(trainerRepository, never()).findNotAssignedToTrainee("Alice.Walker");
    }

    @Test
    void shouldRejectUnauthenticatedToggleActivation() {
        when(authenticationService.matchesTraineeCredentials(auth.username(), auth.password())).thenReturn(false);
        ToggleActivationRequest request = new ToggleActivationRequest("Alice.Walker");

        assertThatThrownBy(() -> traineeService.toggleActivation(auth, request))
                .isInstanceOf(AuthenticationException.class);

        verify(authenticationService, times(1)).matchesTraineeCredentials(auth.username(), auth.password());
        verify(userService, never()).toggleActivation(request);
    }

    @Test
    void shouldDeleteTraineeByUsername() {
        TraineeEntity trainee = TestDataFactory.traineeWithId(1L, "Alice.Walker");
        when(traineeRepository.findByUsername("Alice.Walker")).thenReturn(Optional.of(trainee));

        traineeService.deleteTraineeByUsername(auth, "Alice.Walker");

        verify(authenticationService, times(1)).matchesTraineeCredentials(auth.username(), auth.password());
        verify(traineeRepository, times(1)).findByUsername("Alice.Walker");
        verify(traineeRepository, times(1)).deleteByUsername("Alice.Walker");
    }

    @Test
    void shouldRejectUnauthenticatedTraineeDeletion() {
        when(authenticationService.matchesTraineeCredentials(auth.username(), auth.password())).thenReturn(false);

        assertThatThrownBy(() -> traineeService.deleteTraineeByUsername(auth, "Alice.Walker"))
                .isInstanceOf(AuthenticationException.class);

        verify(authenticationService, times(1)).matchesTraineeCredentials(auth.username(), auth.password());
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
        verify(authenticationService, times(1)).matchesTraineeCredentials(auth.username(), auth.password());
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

        verify(authenticationService, times(1)).matchesTraineeCredentials(auth.username(), auth.password());
        verify(traineeRepository, times(1)).save(trainee);
        assertThat(trainee.getTrainers()).containsExactly(trainer);
    }

    @Test
    void shouldRejectUnauthenticatedTrainersListUpdate() {
        when(authenticationService.matchesTraineeCredentials(auth.username(), auth.password())).thenReturn(false);

        assertThatThrownBy(() -> traineeService.updateTrainersList(auth, "Alice.Walker", Set.of("John.Smith")))
                .isInstanceOf(AuthenticationException.class);

        verify(authenticationService, times(1)).matchesTraineeCredentials(auth.username(), auth.password());
        verify(traineeRepository, never()).findByUsername("Alice.Walker");
    }

    @Test
    void shouldToggleActivation() {
        ToggleActivationRequest request = new ToggleActivationRequest("Alice.Walker");

        traineeService.toggleActivation(auth, request);

        verify(authenticationService, times(1)).matchesTraineeCredentials(auth.username(), auth.password());
        verify(userService, times(1)).toggleActivation(request);
    }

    @Test
    void shouldChangePassword() {
        ChangePasswordRequest request = new ChangePasswordRequest("Alice.Walker", "oldPass1", "NewPass1!");

        traineeService.changePassword(auth, request);

        verify(authenticationService, times(1)).matchesTraineeCredentials(auth.username(), auth.password());
        verify(userService, times(1)).changePassword(request);
    }

    @Test
    void shouldRejectUnauthenticatedChangePassword() {
        when(authenticationService.matchesTraineeCredentials(auth.username(), auth.password())).thenReturn(false);
        ChangePasswordRequest request = new ChangePasswordRequest("Alice.Walker", "oldPass1", "NewPass1!");

        assertThatThrownBy(() -> traineeService.changePassword(auth, request))
                .isInstanceOf(AuthenticationException.class);

        verify(authenticationService, times(1)).matchesTraineeCredentials(auth.username(), auth.password());
        verify(userService, never()).changePassword(request);
    }

    @Test
    void shouldPropagateAuthenticationFailureFromUserServiceOnChangePassword() {
        ChangePasswordRequest request = new ChangePasswordRequest("Alice.Walker", "wrongPass1", "NewPass1!");
        org.mockito.Mockito.doThrow(new AuthenticationException("Invalid credentials"))
                .when(userService).changePassword(request);

        assertThatThrownBy(() -> traineeService.changePassword(auth, request))
                .isInstanceOf(AuthenticationException.class);

        verify(authenticationService, times(1)).matchesTraineeCredentials(auth.username(), auth.password());
        verify(userService, times(1)).changePassword(request);
    }

    @Test
    void shouldThrowWhenDeletingMissingTrainee() {
        when(traineeRepository.findByUsername("Missing.User")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> traineeService.deleteTraineeByUsername(auth, "Missing.User"))
                .isInstanceOf(EntityNotFoundException.class);

        verify(traineeRepository, times(1)).findByUsername("Missing.User");
        verify(traineeRepository, never()).deleteByUsername("Missing.User");
    }

    @Test
    void shouldRejectBlankUsernameOnDelete() {
        assertThatThrownBy(() -> traineeService.deleteTraineeByUsername(auth, " "))
                .isInstanceOf(ValidationException.class);

        verify(traineeRepository, never()).deleteByUsername(any());
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

        verify(authenticationService, times(1)).matchesTraineeCredentials(auth.username(), auth.password());
    }


    @Test
    void traineeMapperShouldMapEntityToResponse() {
        TraineeMapper mapper = new TraineeMapper(org.mockito.Mockito.mock(UsernameGenerator.class), org.mockito.Mockito.mock(PasswordGenerator.class));
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
        UsernameGenerator usernameGenerator = org.mockito.Mockito.mock(UsernameGenerator.class);
        when(usernameGenerator.generateUniqueUsername("Jane", "Doe")).thenReturn("Jane.Doe");
        PasswordGenerator passwordGenerator = org.mockito.Mockito.mock(PasswordGenerator.class);
        when(passwordGenerator.generatePassword()).thenReturn("Pass1234");
        TraineeMapper mapper = new TraineeMapper(usernameGenerator, passwordGenerator);
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
    void traineeMapperShouldMapUpdateRequestOntoExistingEntity() {
        TraineeMapper mapper = new TraineeMapper(
                org.mockito.Mockito.mock(UsernameGenerator.class),
                org.mockito.Mockito.mock(PasswordGenerator.class));
        TraineeEntity existing = TestDataFactory.traineeWithId(3L, "Jane.Doe");
        existing.getUser().setPassword("storedPass");
        existing.getUser().setActive(false);
        UpdateTraineeRequest request = new UpdateTraineeRequest(
                3L, new UserInfo("Jane", "Updated"), LocalDate.of(1990, 1, 1), "Lviv");

        TraineeEntity actual = mapper.toEntity(existing, request);

        assertThat(actual).isNotSameAs(existing);
        assertThat(actual.getUser()).isNotSameAs(existing.getUser());
        assertThat(actual.getId()).isEqualTo(3L);
        assertThat(actual.getUser().getFirstName()).isEqualTo("Jane");
        assertThat(actual.getUser().getLastName()).isEqualTo("Updated");
        assertThat(actual.getUser().getUsername()).isEqualTo("Jane.Doe");
        assertThat(actual.getUser().getPassword()).isEqualTo("storedPass");
        assertThat(actual.getUser().isActive()).isFalse();
        assertThat(actual.getDateOfBirth()).isEqualTo(LocalDate.of(1990, 1, 1));
        assertThat(actual.getAddress()).isEqualTo("Lviv");
        assertThat(existing.getUser().getFirstName()).isNotEqualTo("Jane");
        assertThat(existing.getAddress()).isNotEqualTo("Lviv");
    }
}
