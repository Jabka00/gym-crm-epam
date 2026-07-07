package com.epam.gymcrm.service;

import com.epam.gymcrm.dto.TrainerDto;
import com.epam.gymcrm.entity.TrainerEntity;
import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.exception.InvalidOperationException;
import com.epam.gymcrm.mapper.TrainerMapper;
import com.epam.gymcrm.repository.TrainerRepository;
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
        TrainerDto request = TrainerDto.builder().firstName("John").lastName("Smith").build();

        assertThatThrownBy(() -> trainerService.createTrainer(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Specialization is required");

        verifyNoInteractions(trainerMapper);
        verify(trainerRepository, never()).save(org.mockito.ArgumentMatchers.any());
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
        TrainerDto request = TestDataFactory.trainerDto();
        TrainerEntity mappedEntity = TestDataFactory.createDefaultTrainer();
        TrainerEntity created = TestDataFactory.trainerWithId(1L, "John.Smith");
        TrainerDto expected = TestDataFactory.trainerDtoWithCredentials(1L, "John.Smith");

        when(trainerMapper.toEntity(request)).thenReturn(mappedEntity);
        when(trainerRepository.save(mappedEntity)).thenReturn(created);
        when(trainerMapper.toDto(created)).thenReturn(expected);

        TrainerDto actual = trainerService.createTrainer(request);

        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
        verify(trainerMapper, times(1)).toEntity(request);
        verify(trainerRepository, times(1)).save(mappedEntity);
        verify(trainerMapper, times(1)).toDto(created);
        verifyNoInteractions(authenticationService);
    }

    @Test
    void shouldUpdateTrainer() {
        TrainerDto request = TestDataFactory.trainerDtoWithCredentials(1L, "John.Smith");
        request.getSpecialization().setTypeName("BOXING");
        TrainerEntity existing = TestDataFactory.trainerWithId(1L, "John.Smith");
        TrainerEntity entityToSave = TestDataFactory.trainerWithId(1L, "John.Smith");
        entityToSave.setSpecialization(TestDataFactory.trainingType("BOXING"));
        TrainerDto expected = TestDataFactory.trainerDtoWithCredentials(1L, "John.Smith");
        expected.getSpecialization().setTypeName("BOXING");

        when(trainerMapper.toEntity(request)).thenReturn(entityToSave);
        when(trainerMapper.toDto(entityToSave)).thenReturn(expected);
        when(trainerRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(trainerRepository.save(entityToSave)).thenReturn(entityToSave);

        TrainerDto actual = trainerService.updateTrainer(auth, request);

        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
        verify(trainerRepository, times(1)).findById(1L);
        verify(trainerRepository, times(1)).save(entityToSave);
        verify(authenticationService, times(1)).requireAuthenticated(auth);
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
    }

    @Test
    void shouldReturnOnlyActiveTrainers() {
        TrainerEntity active = TestDataFactory.trainerWithId(1L, "Active.Trainer");
        TrainerEntity inactive = TestDataFactory.trainerWithId(2L, "Inactive.Trainer");
        inactive.setActive(false);
        TrainerDto activeDto = TestDataFactory.trainerDtoWithCredentials(1L, "Active.Trainer");
        when(trainerRepository.findAll()).thenReturn(Stream.of(active, inactive));
        when(trainerMapper.toDto(active)).thenReturn(activeDto);

        List<TrainerDto> actual = trainerService.getAllTrainers();
        List<TrainerDto> expected = List.of(activeDto);

        assertThat(actual)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyElementsOf(expected);
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

        verify(trainerRepository, times(1)).findByUsername("Inactive.Trainer");
        verify(authenticationService, times(1)).requireAuthenticated(auth);
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
        TrainerDto expected = TestDataFactory.trainerDtoWithCredentials(1L, "John.Smith");
        when(trainerRepository.findByUsername("John.Smith")).thenReturn(Optional.of(trainer));
        when(trainerMapper.toDto(trainer)).thenReturn(expected);

        TrainerDto actual = trainerService.getTrainerByUsername(auth, "John.Smith");

        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
        verify(authenticationService, times(1)).requireAuthenticated(auth);
        verify(trainerRepository, times(1)).findByUsername("John.Smith");
    }

    @Test
    void shouldRejectUnauthenticatedUpdateTrainer() {
        TrainerDto request = TestDataFactory.trainerDtoWithCredentials(1L, "John.Smith");
        doThrow(new AuthenticationException("Invalid credentials for username: John.Smith"))
                .when(authenticationService)
                .requireAuthenticated(auth);

        assertThatThrownBy(() -> trainerService.updateTrainer(auth, request))
                .isInstanceOf(AuthenticationException.class);

        verify(authenticationService, times(1)).requireAuthenticated(auth);
        verify(trainerRepository, never()).findById(1L);
        verify(trainerRepository, never()).save(org.mockito.ArgumentMatchers.any());
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
        verify(trainerMapper, never()).toDto(trainer);
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
    }

    @Test
    void shouldFindActiveTrainerBySpecialization() {
        TrainerEntity active = TestDataFactory.trainerWithId(5L, "John.Smith");
        TrainerDto expected = TestDataFactory.trainerDtoWithCredentials(5L, "John.Smith");
        when(trainerRepository.findActiveBySpecialization("YOGA")).thenReturn(Optional.of(active));
        when(trainerMapper.toDto(active)).thenReturn(expected);

        TrainerDto actual = trainerService.findActiveBySpecialization("YOGA");

        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
        verify(trainerRepository, times(1)).findActiveBySpecialization("YOGA");
    }

    @Test
    void shouldThrowWhenNoActiveTrainerForSpecialization() {
        when(trainerRepository.findActiveBySpecialization("YOGA")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainerService.findActiveBySpecialization("YOGA"))
                .isInstanceOf(EntityNotFoundException.class);

        verify(trainerRepository, times(1)).findActiveBySpecialization("YOGA");
    }
}
