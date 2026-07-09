package com.epam.gymcrm.service;

import com.epam.gymcrm.dto.request.CreateTrainerRequest;
import com.epam.gymcrm.dto.request.UpdateTrainerRequest;
import com.epam.gymcrm.dto.request.UserInfo;
import com.epam.gymcrm.dto.response.Trainer;
import com.epam.gymcrm.entity.TrainerEntity;
import com.epam.gymcrm.entity.TrainingTypeEntity;
import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.exception.InvalidOperationException;
import com.epam.gymcrm.mapper.TrainerMapper;
import com.epam.gymcrm.repository.TrainerRepository;
import com.epam.gymcrm.repository.TrainingTypeRepository;
import com.epam.gymcrm.security.Credentials;
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
import java.util.stream.Stream;

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
    }

    @Test
    void shouldRejectCreateWithoutSpecialization() {
        CreateTrainerRequest request = new CreateTrainerRequest(new UserInfo("John", "Smith"), null);

        assertThatThrownBy(() -> trainerService.createTrainer(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Specialization is required");

        verifyNoInteractions(trainerMapper, trainingTypeRepository);
        verify(trainerRepository, never()).save(any());
    }

    @Test
    void shouldVerifyTrainerPassword() {
        when(authenticationService.authenticateTrainer("John.Smith", "pass1234AB")).thenReturn(true);

        assertThat(trainerService.verifyPassword("John.Smith", "pass1234AB")).isTrue();

        verify(authenticationService, times(1)).authenticateTrainer("John.Smith", "pass1234AB");
        verifyNoMoreInteractions(authenticationService);
    }

    @Test
    void shouldRejectInvalidTrainerPassword() {
        when(authenticationService.authenticateTrainer("John.Smith", "wrong")).thenReturn(false);

        assertThat(trainerService.verifyPassword("John.Smith", "wrong")).isFalse();

        verify(authenticationService, times(1)).authenticateTrainer("John.Smith", "wrong");
    }

    @Test
    void shouldCreateTrainer() {
        CreateTrainerRequest request = TestDataFactory.createTrainerRequest("YOGA");
        TrainingTypeEntity specialization = TestDataFactory.yogaTypeEntity();
        TrainerEntity mappedEntity = TestDataFactory.createDefaultTrainer();
        TrainerEntity created = TestDataFactory.trainerWithId(1L, "John.Smith");
        Trainer expected = TestDataFactory.trainerResponse(1L, "John.Smith");

        when(trainingTypeRepository.findByTypeName("YOGA")).thenReturn(Optional.of(specialization));
        when(trainerMapper.toEntity(request, specialization)).thenReturn(mappedEntity);
        when(trainerRepository.save(mappedEntity)).thenReturn(created);
        when(trainerMapper.toResponse(created)).thenReturn(expected);

        Trainer actual = trainerService.createTrainer(request);

        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
        verify(trainingTypeRepository, times(1)).findByTypeName("YOGA");
        verify(trainerMapper, times(1)).toEntity(request, specialization);
        verify(trainerRepository, times(1)).save(mappedEntity);
        verify(trainerMapper, times(1)).toResponse(created);
        verifyNoInteractions(authenticationService);
    }

    @Test
    void shouldUpdateTrainer() {
        UpdateTrainerRequest request = TestDataFactory.updateTrainerRequest(1L, "BOXING");
        TrainingTypeEntity specialization = TestDataFactory.boxingTypeEntity();
        TrainerEntity existing = TestDataFactory.trainerWithId(1L, "John.Smith");
        TrainerEntity entityToSave = TestDataFactory.trainerWithId(1L, "John.Smith");
        entityToSave.setSpecialization(specialization);
        Trainer expected = new Trainer(
                1L,
                "John Smith",
                "John.Smith",
                TestDataFactory.trainingTypeResponse(3L, "BOXING"));

        when(trainingTypeRepository.findByTypeName("BOXING")).thenReturn(Optional.of(specialization));
        when(trainerRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(trainerMapper.toEntity(request, specialization, existing.getUsername(), existing.getPassword()))
                .thenReturn(entityToSave);
        when(trainerRepository.save(entityToSave)).thenReturn(entityToSave);
        when(trainerMapper.toResponse(entityToSave)).thenReturn(expected);

        Trainer actual = trainerService.updateTrainer(auth, request);

        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
        verify(authenticationService, times(1)).requireAuthenticated(auth);
        verify(trainingTypeRepository, times(1)).findByTypeName("BOXING");
        verify(trainerRepository, times(1)).findById(1L);
        verify(trainerMapper, times(1))
                .toEntity(request, specialization, existing.getUsername(), existing.getPassword());
        verify(trainerRepository, times(1)).save(entityToSave);
    }

    @Test
    void shouldGetTrainerById() {
        TrainerEntity trainer = TestDataFactory.trainerWithId(1L, "John.Smith");
        Trainer expected = TestDataFactory.trainerResponse(1L, "John.Smith");
        when(trainerRepository.findById(1L)).thenReturn(Optional.of(trainer));
        when(trainerMapper.toResponse(trainer)).thenReturn(expected);

        Trainer actual = trainerService.getTrainer(1L);

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

        verify(trainerRepository, times(1)).findById(2L);
        verify(trainerMapper, never()).toResponse(trainer);
    }

    @Test
    void shouldReturnOnlyActiveTrainers() {
        TrainerEntity active = TestDataFactory.trainerWithId(1L, "Active.Trainer");
        TrainerEntity inactive = TestDataFactory.trainerWithId(2L, "Inactive.Trainer");
        inactive.setActive(false);
        Trainer activeResponse = TestDataFactory.trainerResponse(1L, "Active.Trainer");
        when(trainerRepository.findAll()).thenReturn(Stream.of(active, inactive));
        when(trainerMapper.toResponse(active)).thenReturn(activeResponse);

        List<Trainer> actual = trainerService.getAllTrainers();

        assertThat(actual)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(activeResponse);
        verify(trainerRepository, times(1)).findAll();
    }

    @Test
    void shouldThrowWhenGettingInactiveTrainerByUsername() {
        TrainerEntity trainer = TestDataFactory.trainerWithId(2L, "Inactive.Trainer");
        trainer.setActive(false);
        when(trainerRepository.findByUsername("Inactive.Trainer")).thenReturn(Optional.of(trainer));

        assertThatThrownBy(() -> trainerService.getTrainerByUsername(auth, "Inactive.Trainer"))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("inactive");

        verify(authenticationService, times(1)).requireAuthenticated(auth);
        verify(trainerRepository, times(1)).findByUsername("Inactive.Trainer");
        verify(trainerMapper, never()).toResponse(trainer);
    }

    @Test
    void shouldRejectUnauthenticatedTrainerLookup() {
        doThrow(new AuthenticationException("Invalid credentials for username: John.Smith"))
                .when(authenticationService)
                .requireAuthenticated(auth);

        assertThatThrownBy(() -> trainerService.getTrainerByUsername(auth, "John.Smith"))
                .isInstanceOf(AuthenticationException.class);

        verify(authenticationService, times(1)).requireAuthenticated(auth);
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
        verify(authenticationService, times(1)).requireAuthenticated(auth);
        verify(trainerRepository, times(1)).findByUsername("John.Smith");
    }

    @Test
    void shouldRejectUnauthenticatedUpdateTrainer() {
        UpdateTrainerRequest request = TestDataFactory.updateTrainerRequest(1L, "YOGA");
        doThrow(new AuthenticationException("Invalid credentials for username: John.Smith"))
                .when(authenticationService)
                .requireAuthenticated(auth);

        assertThatThrownBy(() -> trainerService.updateTrainer(auth, request))
                .isInstanceOf(AuthenticationException.class);

        verify(authenticationService, times(1)).requireAuthenticated(auth);
        verify(trainerRepository, never()).findById(1L);
        verify(trainerRepository, never()).save(any());
    }

    @Test
    void shouldRejectUnauthenticatedToggleActivation() {
        doThrow(new AuthenticationException("Invalid credentials for username: John.Smith"))
                .when(authenticationService)
                .requireAuthenticated(auth);

        assertThatThrownBy(() -> trainerService.toggleActivation(auth, "John.Smith"))
                .isInstanceOf(AuthenticationException.class);

        verify(authenticationService, times(1)).requireAuthenticated(auth);
        verify(userService, never()).toggleActivation("John.Smith");
    }

    @Test
    void shouldDelegateChangePasswordToUserService() {
        trainerService.changePassword(auth, "John.Smith", "oldPass1", "NewPass1!");

        verify(authenticationService, times(1)).requireAuthenticated(auth);
        verify(userService, times(1)).changePassword("John.Smith", "oldPass1", "NewPass1!");
    }

    @Test
    void shouldRejectUnauthenticatedChangePassword() {
        doThrow(new AuthenticationException("Invalid credentials for username: John.Smith"))
                .when(authenticationService)
                .requireAuthenticated(auth);

        assertThatThrownBy(() -> trainerService.changePassword(auth, "John.Smith", "oldPass1", "NewPass1!"))
                .isInstanceOf(AuthenticationException.class);

        verify(authenticationService, times(1)).requireAuthenticated(auth);
        verify(userService, never()).changePassword("John.Smith", "oldPass1", "NewPass1!");
    }

    @Test
    void shouldPropagateAuthenticationFailureFromUserServiceOnChangePassword() {
        doThrow(new AuthenticationException("Invalid credentials for username: John.Smith"))
                .when(userService)
                .changePassword("John.Smith", "wrong", "NewPass1!");

        assertThatThrownBy(() -> trainerService.changePassword(auth, "John.Smith", "wrong", "NewPass1!"))
                .isInstanceOf(AuthenticationException.class);

        verify(authenticationService, times(1)).requireAuthenticated(auth);
        verify(userService, times(1)).changePassword("John.Smith", "wrong", "NewPass1!");
    }

    @Test
    void shouldDelegateToggleActivationToUserService() {
        trainerService.toggleActivation(auth, "John.Smith");

        verify(authenticationService, times(1)).requireAuthenticated(auth);
        verify(userService, times(1)).toggleActivation("John.Smith");
    }

    @Test
    void shouldThrowWhenInactiveTrainerForSpecialization() {
        TrainerEntity trainer = TestDataFactory.trainerWithId(2L, "Inactive.Trainer");
        trainer.setActive(false);
        when(trainerRepository.findById(2L)).thenReturn(Optional.of(trainer));

        assertThatThrownBy(() -> trainerService.getActiveTrainerForSpecialization(2L, "YOGA"))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("inactive");

        verify(trainerRepository, times(1)).findById(2L);
        verify(trainerMapper, never()).toResponse(trainer);
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

        verify(trainerRepository, times(1)).findById(2L);
        verify(trainerMapper, never()).toResponse(trainer);
    }

    @Test
    void shouldReturnActiveTrainerMatchingSpecialization() {
        TrainerEntity trainer = TestDataFactory.trainerWithId(2L, "John.Smith");
        Trainer expected = TestDataFactory.trainerResponse(2L, "John.Smith");
        when(trainerRepository.findById(2L)).thenReturn(Optional.of(trainer));
        when(trainerMapper.toResponse(trainer)).thenReturn(expected);

        Trainer actual = trainerService.getActiveTrainerForSpecialization(2L, "YOGA");

        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
        verify(trainerRepository, times(1)).findById(2L);
        verify(trainerMapper, times(1)).toResponse(trainer);
    }

    @Test
    void shouldThrowWhenTrainerNotFoundById() {
        when(trainerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainerService.getTrainer(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void shouldThrowWhenTrainerNotFoundByUsername() {
        when(trainerRepository.findByUsername("Missing.Trainer")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainerService.getTrainerByUsername(auth, "Missing.Trainer"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void shouldThrowWhenTrainingTypeNotFoundOnCreate() {
        CreateTrainerRequest request = TestDataFactory.createTrainerRequest("UNKNOWN");
        when(trainingTypeRepository.findByTypeName("UNKNOWN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainerService.createTrainer(request))
                .isInstanceOf(EntityNotFoundException.class);

        verify(trainerRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenTrainerNotFoundOnUpdate() {
        UpdateTrainerRequest request = TestDataFactory.updateTrainerRequest(99L, "YOGA");
        when(trainerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainerService.updateTrainer(auth, request))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void trainerEntityShouldMatchSpecializationCaseInsensitively() {
        TrainerEntity trainer = TestDataFactory.trainerWithId(1L, "John.Smith");
        trainer.setSpecialization(TestDataFactory.trainingType("YOGA"));

        assertThat(trainer.matchesSpecialization("yoga")).isTrue();
        assertThat(trainer.matchesSpecialization("YOGA")).isTrue();
        assertThat(trainer.matchesSpecialization("BOXING")).isFalse();
        assertThat(trainer.matchesSpecialization(null)).isFalse();
        assertThat(trainer.matchesSpecialization(" ")).isFalse();
    }

    @Test
    void trainerMapperShouldMapEntityToResponse() {
        TrainerMapper mapper = new TrainerMapper(org.mockito.Mockito.mock(UserCredentialService.class));
        TrainerEntity entity = TestDataFactory.trainerWithId(2L, "John.Smith");
        entity.setFirstName("John");
        entity.setLastName("Smith");

        Trainer actual = mapper.toResponse(entity);

        assertThat(actual.userId()).isEqualTo(2L);
        assertThat(actual.fullName()).isEqualTo("John Smith");
        assertThat(actual.username()).isEqualTo("John.Smith");
        assertThat(actual.specialization().typeName()).isEqualTo("YOGA");
    }

    @Test
    void trainerMapperShouldMapCreateRequestToEntity() {
        UserCredentialService credentialService = org.mockito.Mockito.mock(UserCredentialService.class);
        when(credentialService.generateUniqueUsername("John", "Smith")).thenReturn("John.Smith");
        when(credentialService.generatePassword()).thenReturn("Pass1234");
        TrainerMapper mapper = new TrainerMapper(credentialService);
        CreateTrainerRequest request = TestDataFactory.createTrainerRequest("YOGA");
        TrainingTypeEntity specialization = TestDataFactory.yogaTypeEntity();

        TrainerEntity actual = mapper.toEntity(request, specialization);

        assertThat(actual.getUsername()).isEqualTo("John.Smith");
        assertThat(actual.getPassword()).isEqualTo("Pass1234");
        assertThat(actual.isActive()).isTrue();
        assertThat(actual.getSpecialization()).isEqualTo(specialization);
    }

    @Test
    void trainerMapperShouldMapTrainingTypeEntityToResponse() {
        TrainerMapper mapper = new TrainerMapper(org.mockito.Mockito.mock(UserCredentialService.class));

        assertThat(mapper.toTrainingTypeResponse(TestDataFactory.yogaTypeEntity()))
                .isEqualTo(TestDataFactory.trainingTypeResponse(1L, "YOGA"));
    }
}
