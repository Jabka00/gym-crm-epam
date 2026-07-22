package com.epam.gymcrm.service;

import com.epam.gymcrm.service.UsernameGenerator;

import com.epam.gymcrm.service.PasswordGenerator;

import com.epam.gymcrm.dto.request.CreateTrainerRequest;
import com.epam.gymcrm.dto.request.UpdateTrainerRequest;
import com.epam.gymcrm.dto.request.UserInfo;
import com.epam.gymcrm.dto.Trainer;
import com.epam.gymcrm.entity.TrainerEntity;
import com.epam.gymcrm.entity.TrainingTypeEntity;
import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.exception.InvalidOperationException;
import com.epam.gymcrm.exception.ValidationException;
import com.epam.gymcrm.mapper.TrainerMapper;
import com.epam.gymcrm.model.TrainingType;
import com.epam.gymcrm.repository.TrainerRepository;
import com.epam.gymcrm.repository.TrainingTypeRepository;
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

import java.util.Optional;

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
class TrainerServiceTest {

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private TrainingTypeRepository trainingTypeRepository;

    @Mock
    private UserService userService;

    @Mock
    private AuthenticationService authenticationService;

    @Spy
    private DtoValidator dtoValidator = new DtoValidator();

    @Mock
    private TrainerMapper trainerMapper;

    @InjectMocks
    private TrainerService trainerService;

    private Credentials auth;

    @BeforeEach
    void setUp() {
        auth = TestDataFactory.credentials();
        lenient().when(authenticationService.matchesTrainerCredentials(auth.username(), auth.password()))
                .thenReturn(true);
    }

    @Test
    void shouldRejectCreateWithoutSpecialization() {
        CreateTrainerRequest request = new CreateTrainerRequest(new UserInfo("John", "Smith"), null);

        assertThatThrownBy(() -> trainerService.createTrainer(request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Specialization is required");

        verifyNoInteractions(trainerMapper, trainingTypeRepository);
        verify(trainerRepository, never()).save(any());
    }

    @Test
    void shouldCreateTrainer() {
        CreateTrainerRequest request = TestDataFactory.createTrainerRequest(TrainingType.YOGA);
        TrainingTypeEntity specialization = TestDataFactory.yogaTypeEntity();
        TrainerEntity mappedEntity = TestDataFactory.createDefaultTrainer();
        TrainerEntity created = TestDataFactory.trainerWithId(1L, "John.Smith");
        Trainer expected = TestDataFactory.trainerResponse(1L, "John.Smith");

        when(trainingTypeRepository.findByTypeName(TrainingType.YOGA)).thenReturn(Optional.of(specialization));
        when(trainerMapper.toEntity(request, specialization)).thenReturn(mappedEntity);
        when(trainerRepository.save(mappedEntity)).thenReturn(created);
        when(trainerMapper.toResponse(created)).thenReturn(expected);

        Trainer actual = trainerService.createTrainer(request);

        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
        verify(trainingTypeRepository, times(1)).findByTypeName(TrainingType.YOGA);
        verify(trainerMapper, times(1)).toEntity(request, specialization);
        verify(trainerRepository, times(1)).save(mappedEntity);
        verify(trainerMapper, times(1)).toResponse(created);
        verifyNoInteractions(authenticationService);
    }

    @Test
    void shouldUpdateTrainer() {
        UpdateTrainerRequest request = TestDataFactory.updateTrainerRequest(1L, TrainingType.BOXING);
        TrainingTypeEntity specialization = TestDataFactory.boxingTypeEntity();
        TrainerEntity existing = TestDataFactory.trainerWithId(1L, "John.Smith");
        TrainerEntity entityToSave = TestDataFactory.trainerWithId(1L, "John.Smith");
        entityToSave.setSpecialization(specialization);
        Trainer expected = new Trainer(
                1L,
                "John Smith",
                "John.Smith",
                TrainingType.BOXING);

        when(trainingTypeRepository.findByTypeName(TrainingType.BOXING)).thenReturn(Optional.of(specialization));
        when(trainerRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(trainerMapper.toEntity(existing, request, specialization)).thenReturn(entityToSave);
        when(trainerRepository.save(entityToSave)).thenReturn(entityToSave);
        when(trainerMapper.toResponse(entityToSave)).thenReturn(expected);

        Trainer actual = trainerService.updateTrainer(auth, request);

        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
        verify(authenticationService, times(1)).matchesTrainerCredentials(auth.username(), auth.password());
        verify(trainingTypeRepository, times(1)).findByTypeName(TrainingType.BOXING);
        verify(trainerRepository, times(1)).findById(1L);
        verify(trainerMapper, times(1)).toEntity(existing, request, specialization);
        verify(trainerRepository, times(1)).save(entityToSave);
    }

    @Test
    void shouldThrowWhenGettingInactiveTrainerByUsername() {
        TrainerEntity trainer = TestDataFactory.trainerWithId(2L, "Inactive.Trainer");
        trainer.getUser().setActive(false);
        when(trainerRepository.findByUsername("Inactive.Trainer")).thenReturn(Optional.of(trainer));

        assertThatThrownBy(() -> trainerService.getTrainerByUsername(auth, "Inactive.Trainer"))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("inactive");

        verify(authenticationService, times(1)).matchesTrainerCredentials(auth.username(), auth.password());
        verify(trainerRepository, times(1)).findByUsername("Inactive.Trainer");
        verify(trainerMapper, never()).toResponse(trainer);
    }

    @Test
    void shouldRejectUnauthenticatedTrainerLookup() {
        when(authenticationService.matchesTrainerCredentials(auth.username(), auth.password())).thenReturn(false);

        assertThatThrownBy(() -> trainerService.getTrainerByUsername(auth, "John.Smith"))
                .isInstanceOf(AuthenticationException.class);

        verify(authenticationService, times(1)).matchesTrainerCredentials(auth.username(), auth.password());
        verify(trainerRepository, never()).findByUsername("John.Smith");
    }

    @Test
    void shouldGetTrainerByUsernameWhenAuthenticated() {
        TrainerEntity trainer = TestDataFactory.trainerWithId(1L, "John.Smith");
        Trainer expected = TestDataFactory.trainerResponse(1L, "John.Smith");
        when(trainerRepository.findByUsername("John.Smith")).thenReturn(Optional.of(trainer));
        when(trainerMapper.toResponse(trainer)).thenReturn(expected);

        Trainer actual = trainerService.getTrainerByUsername(auth, "John.Smith");

        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
        verify(authenticationService, times(1)).matchesTrainerCredentials(auth.username(), auth.password());
        verify(trainerRepository, times(1)).findByUsername("John.Smith");
    }

    @Test
    void shouldRejectUnauthenticatedUpdateTrainer() {
        UpdateTrainerRequest request = TestDataFactory.updateTrainerRequest(1L, TrainingType.YOGA);
        when(authenticationService.matchesTrainerCredentials(auth.username(), auth.password())).thenReturn(false);

        assertThatThrownBy(() -> trainerService.updateTrainer(auth, request))
                .isInstanceOf(AuthenticationException.class);

        verify(authenticationService, times(1)).matchesTrainerCredentials(auth.username(), auth.password());
        verify(trainerRepository, never()).findById(1L);
        verify(trainerRepository, never()).save(any());
    }

    @Test
    void shouldRejectUnauthenticatedToggleActivation() {
        when(authenticationService.matchesTrainerCredentials(auth.username(), auth.password())).thenReturn(false);
        ToggleActivationRequest request = new ToggleActivationRequest("John.Smith");

        assertThatThrownBy(() -> trainerService.toggleActivation(auth, request))
                .isInstanceOf(AuthenticationException.class);

        verify(authenticationService, times(1)).matchesTrainerCredentials(auth.username(), auth.password());
        verify(userService, never()).toggleActivation(request);
    }

    @Test
    void shouldChangePassword() {
        ChangePasswordRequest request = new ChangePasswordRequest("John.Smith", "oldPass1", "NewPass1!");

        trainerService.changePassword(auth, request);

        verify(authenticationService, times(1)).matchesTrainerCredentials(auth.username(), auth.password());
        verify(userService, times(1)).changePassword(request);
    }

    @Test
    void shouldRejectUnauthenticatedChangePassword() {
        when(authenticationService.matchesTrainerCredentials(auth.username(), auth.password())).thenReturn(false);
        ChangePasswordRequest request = new ChangePasswordRequest("John.Smith", "oldPass1", "NewPass1!");

        assertThatThrownBy(() -> trainerService.changePassword(auth, request))
                .isInstanceOf(AuthenticationException.class);

        verify(authenticationService, times(1)).matchesTrainerCredentials(auth.username(), auth.password());
        verify(userService, never()).changePassword(request);
    }

    @Test
    void shouldPropagateAuthenticationFailureFromUserServiceOnChangePassword() {
        ChangePasswordRequest request = new ChangePasswordRequest("John.Smith", "wrongPass1", "NewPass1!");
        org.mockito.Mockito.doThrow(new AuthenticationException("Invalid credentials"))
                .when(userService).changePassword(request);

        assertThatThrownBy(() -> trainerService.changePassword(auth, request))
                .isInstanceOf(AuthenticationException.class);

        verify(authenticationService, times(1)).matchesTrainerCredentials(auth.username(), auth.password());
        verify(userService, times(1)).changePassword(request);
    }

    @Test
    void shouldToggleActivation() {
        ToggleActivationRequest request = new ToggleActivationRequest("John.Smith");

        trainerService.toggleActivation(auth, request);

        verify(authenticationService, times(1)).matchesTrainerCredentials(auth.username(), auth.password());
        verify(userService, times(1)).toggleActivation(request);
    }

    @Test
    void shouldThrowWhenTrainerNotFoundByUsername() {
        when(trainerRepository.findByUsername("Missing.Trainer")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainerService.getTrainerByUsername(auth, "Missing.Trainer"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void shouldThrowWhenTrainingTypeNotFoundOnCreate() {
        CreateTrainerRequest request = TestDataFactory.createTrainerRequest(TrainingType.PILATES);
        when(trainingTypeRepository.findByTypeName(TrainingType.PILATES)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainerService.createTrainer(request))
                .isInstanceOf(EntityNotFoundException.class);

        verify(trainerRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenTrainerNotFoundOnUpdate() {
        UpdateTrainerRequest request = TestDataFactory.updateTrainerRequest(99L, TrainingType.YOGA);
        when(trainerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainerService.updateTrainer(auth, request))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void trainerMapperShouldMapEntityToResponse() {
        TrainerMapper mapper = new TrainerMapper(
                org.mockito.Mockito.mock(UsernameGenerator.class),
                org.mockito.Mockito.mock(PasswordGenerator.class));
        TrainerEntity entity = TestDataFactory.trainerWithId(2L, "John.Smith");
        entity.getUser().setFirstName("John");
        entity.getUser().setLastName("Smith");

        Trainer actual = mapper.toResponse(entity);

        assertThat(actual.userId()).isEqualTo(2L);
        assertThat(actual.fullName()).isEqualTo("John Smith");
        assertThat(actual.username()).isEqualTo("John.Smith");
        assertThat(actual.specialization()).isEqualTo(TrainingType.YOGA);
    }

    @Test
    void trainerMapperShouldMapCreateRequestToEntity() {
        UsernameGenerator usernameGenerator = org.mockito.Mockito.mock(UsernameGenerator.class);
        PasswordGenerator passwordGenerator = org.mockito.Mockito.mock(PasswordGenerator.class);
        when(usernameGenerator.generateUniqueUsername("John", "Smith")).thenReturn("John.Smith");
        when(passwordGenerator.generatePassword()).thenReturn("Pass1234");
        TrainerMapper mapper = new TrainerMapper(usernameGenerator, passwordGenerator);
        CreateTrainerRequest request = TestDataFactory.createTrainerRequest(TrainingType.YOGA);
        TrainingTypeEntity specialization = TestDataFactory.yogaTypeEntity();

        TrainerEntity actual = mapper.toEntity(request, specialization);

        assertThat(actual.getUser().getUsername()).isEqualTo("John.Smith");
        assertThat(actual.getUser().getPassword()).isEqualTo("Pass1234");
        assertThat(actual.getUser().isActive()).isTrue();
        assertThat(actual.getSpecialization()).isEqualTo(specialization);
    }

    @Test
    void trainerMapperShouldMapUpdateRequestOntoExistingEntity() {
        TrainerMapper mapper = new TrainerMapper(
                org.mockito.Mockito.mock(UsernameGenerator.class),
                org.mockito.Mockito.mock(PasswordGenerator.class));
        TrainerEntity existing = TestDataFactory.trainerWithId(2L, "John.Smith");
        existing.getUser().setPassword("storedPass");
        existing.getUser().setActive(false);
        UpdateTrainerRequest request = TestDataFactory.updateTrainerRequest(2L, TrainingType.BOXING);
        TrainingTypeEntity specialization = TestDataFactory.boxingTypeEntity();

        TrainerEntity actual = mapper.toEntity(existing, request, specialization);

        assertThat(actual).isNotSameAs(existing);
        assertThat(actual.getUser()).isNotSameAs(existing.getUser());
        assertThat(actual.getUser().getFirstName()).isEqualTo("John");
        assertThat(actual.getUser().getLastName()).isEqualTo("Smith");
        assertThat(actual.getUser().getUsername()).isEqualTo("John.Smith");
        assertThat(actual.getUser().getPassword()).isEqualTo("storedPass");
        assertThat(actual.getUser().isActive()).isFalse();
        assertThat(actual.getSpecialization()).isEqualTo(specialization);
    }
}
