package com.epam.gymcrm.service;

import com.epam.gymcrm.dto.TrainerDto;
import com.epam.gymcrm.entity.TrainerEntity;
import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.exception.InvalidOperationException;
import com.epam.gymcrm.mapper.TrainerMapper;
import com.epam.gymcrm.repository.TrainerRepository;
import com.epam.gymcrm.security.AuthenticationGuard;
import com.epam.gymcrm.security.Credentials;
import com.epam.gymcrm.support.MapperTestSupport;
import com.epam.gymcrm.support.TestDataFactory;
import com.epam.gymcrm.util.UserInitializationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
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
class TrainerServiceTest {

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private UserInitializationUtil userInitializationUtil;

    @Mock
    private UserService userService;

    @Mock
    private AuthenticationGuard authenticationGuard;

    @Spy
    private TrainerMapper trainerMapper = MapperTestSupport.trainerMapper();

    @InjectMocks
    private TrainerService trainerService;

    private Credentials auth;

    @BeforeEach
    void setUp() {
        auth = TestDataFactory.credentials();
        doNothing().when(authenticationGuard).ensureAuthenticated(any(Credentials.class));
    }

    @Test
    void shouldCreateTrainer() {
        TrainerDto request = TestDataFactory.trainerDto();
        TrainerEntity mappedEntity = trainerMapper.toEntity(request);
        TrainerEntity created = TestDataFactory.trainerWithId(1L, "John.Smith");
        TrainerDto expected = trainerMapper.toDto(created);

        doAnswer(invocation -> {
            TrainerEntity entity = invocation.getArgument(0);
            UnaryOperator<TrainerEntity> saver = invocation.getArgument(1);
            when(trainerRepository.save(entity)).thenReturn(created);
            return saver.apply(entity);
        }).when(userInitializationUtil).createUser(
                argThat(entity -> matchesTrainerEntity(entity, mappedEntity)),
                argThat(TrainerServiceTest::isUnaryOperator),
                eq("Trainer"));

        TrainerDto actual = trainerService.createTrainer(request);

        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);

        ArgumentCaptor<TrainerEntity> entityCaptor = ArgumentCaptor.forClass(TrainerEntity.class);
        verify(userInitializationUtil, times(1))
                .createUser(entityCaptor.capture(), argThat(TrainerServiceTest::isUnaryOperator), eq("Trainer"));
        assertThat(entityCaptor.getValue())
                .usingRecursiveComparison()
                .ignoringFields("id", "username", "password", "active", "trainees", "trainings", "specialization")
                .isEqualTo(mappedEntity);
        verify(trainerRepository, times(1)).save(entityCaptor.getValue());
        verify(authenticationGuard, never()).ensureAuthenticated(any());
    }

    @Test
    void shouldUpdateTrainer() {
        TrainerDto request = TestDataFactory.trainerDtoWithCredentials(1L, "John.Smith");
        request.getSpecialization().setTypeName("BOXING");
        TrainerEntity existing = TestDataFactory.trainerWithId(1L, "John.Smith");
        TrainerEntity entityToSave = trainerMapper.toEntity(request);
        TrainerDto expected = trainerMapper.toDto(entityToSave);

        when(trainerRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(trainerRepository.save(argThat(entity -> matchesTrainerEntity(entity, entityToSave))))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TrainerDto actual = trainerService.updateTrainer(auth, request);

        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
        verify(trainerRepository, times(1)).findById(1L);

        ArgumentCaptor<TrainerEntity> saveCaptor = ArgumentCaptor.forClass(TrainerEntity.class);
        verify(trainerRepository, times(1)).save(saveCaptor.capture());
        assertThat(saveCaptor.getValue()).usingRecursiveComparison()
                .ignoringFields("trainees", "trainings")
                .isEqualTo(entityToSave);
        verify(authenticationGuard, times(1)).ensureAuthenticated(auth);
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

        verify(trainerRepository, times(1)).findById(2L);
    }

    @Test
    void shouldReturnOnlyActiveTrainers() {
        TrainerEntity active = TestDataFactory.trainerWithId(1L, "Active.Trainer");
        TrainerEntity inactive = TestDataFactory.trainerWithId(2L, "Inactive.Trainer");
        inactive.setActive(false);
        when(trainerRepository.findAll()).thenReturn(Stream.of(active, inactive));

        List<TrainerDto> actual = trainerService.getAllTrainers();
        List<TrainerDto> expected = List.of(trainerMapper.toDto(active));

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
        verify(authenticationGuard, times(1)).ensureAuthenticated(auth);
    }

    @Test
    void shouldRejectUnauthenticatedTrainerLookup() {
        doThrow(new AuthenticationException("Invalid credentials for username: John.Smith"))
                .when(authenticationGuard)
                .ensureAuthenticated(auth);

        assertThatThrownBy(() -> trainerService.getTrainerByUsername(auth, "John.Smith"))
                .isInstanceOf(AuthenticationException.class);

        verify(authenticationGuard, times(1)).ensureAuthenticated(auth);
        verify(trainerRepository, never()).findByUsername(any());
    }

    @Test
    void shouldGetTrainerByUsernameWhenAuthenticated() {
        TrainerEntity trainer = TestDataFactory.trainerWithId(1L, "John.Smith");
        TrainerDto expected = trainerMapper.toDto(trainer);
        when(trainerRepository.findByUsername("John.Smith")).thenReturn(Optional.of(trainer));

        TrainerDto actual = trainerService.getTrainerByUsername(auth, "John.Smith");

        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
        verify(authenticationGuard, times(1)).ensureAuthenticated(auth);
        verify(trainerRepository, times(1)).findByUsername("John.Smith");
    }

    @Test
    void shouldRejectUnauthenticatedUpdateTrainer() {
        TrainerDto request = TestDataFactory.trainerDtoWithCredentials(1L, "John.Smith");
        doThrow(new AuthenticationException("Invalid credentials for username: John.Smith"))
                .when(authenticationGuard)
                .ensureAuthenticated(auth);

        assertThatThrownBy(() -> trainerService.updateTrainer(auth, request))
                .isInstanceOf(AuthenticationException.class);

        verify(authenticationGuard, times(1)).ensureAuthenticated(auth);
        verify(trainerRepository, never()).findById(any());
        verify(trainerRepository, never()).save(any());
    }

    @Test
    void shouldRejectUnauthenticatedToggleActivation() {
        doThrow(new AuthenticationException("Invalid credentials for username: John.Smith"))
                .when(authenticationGuard)
                .ensureAuthenticated(auth);

        assertThatThrownBy(() -> trainerService.toggleActivation(auth, "John.Smith"))
                .isInstanceOf(AuthenticationException.class);

        verify(authenticationGuard, times(1)).ensureAuthenticated(auth);
        verify(userService, never()).toggleActivation(any());
    }

    @Test
    void shouldDelegateChangePasswordToUserService() {
        trainerService.changePassword("John.Smith", "oldPass1", "NewPass1!");

        verify(userService, times(1)).changePassword("John.Smith", "oldPass1", "NewPass1!");
    }

    @Test
    void shouldDelegateToggleActivationToUserService() {
        trainerService.toggleActivation(auth, "John.Smith");

        verify(authenticationGuard, times(1)).ensureAuthenticated(auth);
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
        verify(trainerMapper, never()).toDto(eq(trainer));
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
        TrainerDto expected = trainerMapper.toDto(active);
        when(trainerRepository.findActiveBySpecialization("YOGA")).thenReturn(Optional.of(active));

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

    private static boolean matchesTrainerEntity(TrainerEntity actual, TrainerEntity expected) {
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
