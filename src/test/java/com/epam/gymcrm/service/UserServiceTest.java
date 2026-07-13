package com.epam.gymcrm.service;

import com.epam.gymcrm.entity.TraineeEntity;
import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.repository.TraineeRepository;
import com.epam.gymcrm.repository.TrainerRepository;
import com.epam.gymcrm.support.TestDataFactory;
import com.epam.gymcrm.util.PasswordValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private PasswordValidator passwordValidator;

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldChangePasswordWhenOldPasswordIsValid() {
        TraineeEntity trainee = TestDataFactory.traineeWithId(1L, "Alice.Walker");
        when(authenticationService.authenticate("Alice.Walker", "secret1234")).thenReturn(true);
        when(traineeRepository.findByUsername("Alice.Walker")).thenReturn(Optional.of(trainee));
        when(traineeRepository.save(trainee)).thenReturn(trainee);

        userService.changePassword("Alice.Walker", "secret1234", "NewPass1!");

        verify(passwordValidator, times(1)).validate("NewPass1!");
        verify(authenticationService, times(1)).authenticate("Alice.Walker", "secret1234");
        verify(traineeRepository, times(1)).save(trainee);
    }

    @Test
    void shouldRejectPasswordChangeWhenOldPasswordIsInvalid() {
        when(authenticationService.authenticate("Alice.Walker", "wrong")).thenReturn(false);

        assertThatThrownBy(() -> userService.changePassword("Alice.Walker", "wrong", "NewPass1!"))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Invalid credentials for username: Alice.Walker");

        verify(passwordValidator, times(1)).validate("NewPass1!");
        verify(authenticationService, times(1)).authenticate("Alice.Walker", "wrong");
        verify(traineeRepository, never()).findByUsername(eq("Alice.Walker"));
        verify(traineeRepository, never()).save(any());
        verify(trainerRepository, never()).findByUsername(any());
    }

    @Test
    void shouldThrowWhenUserNotFoundDuringPasswordChange() {
        when(authenticationService.authenticate("Missing.User", "secret1234")).thenReturn(true);
        when(traineeRepository.findByUsername("Missing.User")).thenReturn(Optional.empty());
        when(trainerRepository.findByUsername("Missing.User")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.changePassword("Missing.User", "secret1234", "NewPass1!"))
                .isInstanceOf(EntityNotFoundException.class);

        verify(traineeRepository, times(1)).findByUsername("Missing.User");
        verify(trainerRepository, times(1)).findByUsername("Missing.User");
    }

    @Test
    void shouldToggleTraineeActivation() {
        TraineeEntity trainee = TestDataFactory.traineeWithId(1L, "Alice.Walker");
        trainee.getUser().setActive(true);
        when(traineeRepository.findByUsername("Alice.Walker")).thenReturn(Optional.of(trainee));
        when(traineeRepository.save(trainee)).thenReturn(trainee);

        userService.toggleActivation("Alice.Walker");

        assertThat(trainee.getUser().isActive()).isFalse();
        verify(traineeRepository, times(1)).save(trainee);
    }

    @Test
    void shouldToggleTrainerActivation() {
        when(traineeRepository.findByUsername("John.Smith")).thenReturn(Optional.empty());
        var trainer = TestDataFactory.trainerWithId(2L, "John.Smith");
        trainer.getUser().setActive(true);
        when(trainerRepository.findByUsername("John.Smith")).thenReturn(Optional.of(trainer));
        when(trainerRepository.save(trainer)).thenReturn(trainer);

        userService.toggleActivation("John.Smith");

        assertThat(trainer.getUser().isActive()).isFalse();
        verify(trainerRepository, times(1)).save(trainer);
    }

    @Test
    void shouldThrowWhenTogglingActivationForMissingUser() {
        when(traineeRepository.findByUsername("Missing.User")).thenReturn(Optional.empty());
        when(trainerRepository.findByUsername("Missing.User")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.toggleActivation("Missing.User"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void shouldRejectBlankUsernameOnToggleActivation() {
        assertThatThrownBy(() -> userService.toggleActivation(" "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldChangeTrainerPasswordWhenOldPasswordIsValid() {
        var trainer = TestDataFactory.trainerWithId(2L, "John.Smith");
        when(authenticationService.authenticate("John.Smith", "secret1234")).thenReturn(true);
        when(traineeRepository.findByUsername("John.Smith")).thenReturn(Optional.empty());
        when(trainerRepository.findByUsername("John.Smith")).thenReturn(Optional.of(trainer));
        when(trainerRepository.save(trainer)).thenReturn(trainer);

        userService.changePassword("John.Smith", "secret1234", "NewPass1!");

        verify(trainerRepository, times(1)).save(trainer);
    }

    @Test
    void shouldRejectNullPassword() {
        PasswordValidator passwordValidator = new PasswordValidator(
                "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).{8,}$",
                "Password must be at least 8 characters and contain uppercase, lowercase, and digit");

        assertThatThrownBy(() -> passwordValidator.validate(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password cannot be null");
    }

    @Test
    void shouldRejectWeakPassword() {
        PasswordValidator passwordValidator = new PasswordValidator(
                "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).{8,}$",
                "Password must be at least 8 characters and contain uppercase, lowercase, and digit");

        assertThatThrownBy(() -> passwordValidator.validate("weak"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password must be at least 8 characters and contain uppercase, lowercase, and digit");
    }

    @Test
    void shouldAcceptValidPassword() {
        PasswordValidator passwordValidator = new PasswordValidator(
                "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).{8,}$",
                "Password must be at least 8 characters and contain uppercase, lowercase, and digit");

        passwordValidator.validate("ValidPass1");
    }
}
