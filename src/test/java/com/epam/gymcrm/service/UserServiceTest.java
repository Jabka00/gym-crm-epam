package com.epam.gymcrm.service;

import com.epam.gymcrm.dto.request.ChangePasswordRequest;
import com.epam.gymcrm.dto.request.ToggleActivationRequest;
import com.epam.gymcrm.entity.UserEntity;
import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.exception.ValidationException;
import com.epam.gymcrm.model.AuthenticationResult;
import com.epam.gymcrm.repository.UserRepository;
import com.epam.gymcrm.support.TestDataFactory;
import com.epam.gymcrm.util.DtoValidator;
import com.epam.gymcrm.util.PasswordValidator;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordValidator passwordValidator;

    @Mock
    private AuthenticationService authenticationService;

    @Spy
    private DtoValidator dtoValidator = new DtoValidator();

    @InjectMocks
    private UserService userService;

    @Test
    void shouldChangePasswordWhenOldPasswordIsValid() {
        UserEntity user = TestDataFactory.traineeWithId(1L, "Alice.Walker").getUser();
        when(authenticationService.authenticate("Alice.Walker", "secret1234"))
                .thenReturn(AuthenticationResult.SUCCESS);
        when(userRepository.findByUsername("Alice.Walker")).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        userService.changePassword(new ChangePasswordRequest("Alice.Walker", "secret1234", "NewPass1!"));

        verify(passwordValidator, times(1)).validate("NewPass1!");
        verify(authenticationService, times(1)).authenticate("Alice.Walker", "secret1234");
        verify(userRepository, times(1)).save(user);
        assertThat(user.getPassword()).isEqualTo("NewPass1!");
    }

    @Test
    void shouldRejectPasswordChangeWhenOldPasswordIsInvalid() {
        when(authenticationService.authenticate("Alice.Walker", "wrongPass1"))
                .thenReturn(AuthenticationResult.FAILURE);

        assertThatThrownBy(() -> userService.changePassword(
                        new ChangePasswordRequest("Alice.Walker", "wrongPass1", "NewPass1!")))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Invalid credentials");

        verify(passwordValidator, times(1)).validate("NewPass1!");
        verify(userRepository, never()).findByUsername(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenUserNotFoundDuringPasswordChange() {
        when(authenticationService.authenticate("Missing.User", "secret1234"))
                .thenReturn(AuthenticationResult.SUCCESS);
        when(userRepository.findByUsername("Missing.User")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.changePassword(
                        new ChangePasswordRequest("Missing.User", "secret1234", "NewPass1!")))
                .isInstanceOf(EntityNotFoundException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldRejectBlankUsernameOnChangePassword() {
        assertThatThrownBy(() -> userService.changePassword(
                        new ChangePasswordRequest(" ", "secret1234", "NewPass1!")))
                .isInstanceOf(ValidationException.class);

        verify(passwordValidator, never()).validate(any());
        verify(authenticationService, never()).authenticate(any(), any());
    }

    @Test
    void shouldToggleActivation() {
        UserEntity user = TestDataFactory.traineeWithId(1L, "Alice.Walker").getUser();
        user.setActive(true);
        when(userRepository.findByUsername("Alice.Walker")).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        userService.toggleActivation(new ToggleActivationRequest("Alice.Walker"));

        assertThat(user.isActive()).isFalse();
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void shouldThrowWhenTogglingActivationForMissingUser() {
        when(userRepository.findByUsername("Missing.User")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.toggleActivation(new ToggleActivationRequest("Missing.User")))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void shouldRejectBlankUsernameOnToggleActivation() {
        assertThatThrownBy(() -> userService.toggleActivation(new ToggleActivationRequest(" ")))
                .isInstanceOf(ValidationException.class);

        verify(userRepository, never()).findByUsername(any());
    }
}
