package com.epam.gymcrm.service;

import com.epam.gymcrm.dto.request.ChangePasswordRequest;
import com.epam.gymcrm.dto.request.ToggleActivationRequest;
import com.epam.gymcrm.entity.UserEntity;
import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.exception.ValidationException;
import com.epam.gymcrm.repository.UserRepository;
import com.epam.gymcrm.support.TestDataFactory;
import com.epam.gymcrm.util.DtoValidator;
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

    @Spy
    private DtoValidator dtoValidator = new DtoValidator();

    @InjectMocks
    private UserService userService;

    @Test
    void shouldChangePasswordWhenOldPasswordIsValid() {
        UserEntity user = TestDataFactory.traineeWithId(1L, "Alice.Walker").getUser();
        user.setPassword("secret1234");
        when(userRepository.findByUsername("Alice.Walker")).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        userService.changePassword(new ChangePasswordRequest("Alice.Walker", "secret1234", "NewPass1word"));

        verify(userRepository, times(1)).findByUsername("Alice.Walker");
        verify(userRepository, times(1)).save(user);
        assertThat(user.getPassword()).isEqualTo("NewPass1word");
    }

    @Test
    void shouldRejectPasswordChangeWhenOldPasswordIsInvalid() {
        UserEntity user = TestDataFactory.traineeWithId(1L, "Alice.Walker").getUser();
        user.setPassword("correctPass1");
        when(userRepository.findByUsername("Alice.Walker")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.changePassword(
                        new ChangePasswordRequest("Alice.Walker", "wrongPass1", "NewPass1word")))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Invalid credentials");

        verify(userRepository, times(1)).findByUsername("Alice.Walker");
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldRejectWeakNewPassword() {
        assertThatThrownBy(() -> userService.changePassword(
                        new ChangePasswordRequest("Alice.Walker", "secret1234", "weak")))
                .isInstanceOf(ValidationException.class);

        verify(userRepository, never()).findByUsername(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenUserNotFoundDuringPasswordChange() {
        when(userRepository.findByUsername("Missing.User")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.changePassword(
                        new ChangePasswordRequest("Missing.User", "secret1234", "NewPass1word")))
                .isInstanceOf(EntityNotFoundException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldRejectBlankUsernameOnChangePassword() {
        assertThatThrownBy(() -> userService.changePassword(
                        new ChangePasswordRequest(" ", "secret1234", "NewPass1word")))
                .isInstanceOf(ValidationException.class);

        verify(userRepository, never()).findByUsername(any());
    }

    @Test
    void shouldToggleActivation() {
        UserEntity user = TestDataFactory.traineeWithId(1L, "Alice.Walker").getUser();
        user.setActive(true);
        when(userRepository.findByUsername("Alice.Walker")).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        userService.toggleActivation(new ToggleActivationRequest("Alice.Walker", false));

        assertThat(user.isActive()).isFalse();
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void shouldThrowWhenTogglingActivationForMissingUser() {
        when(userRepository.findByUsername("Missing.User")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.toggleActivation(new ToggleActivationRequest("Missing.User", true)))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void shouldRejectBlankUsernameOnToggleActivation() {
        assertThatThrownBy(() -> userService.toggleActivation(new ToggleActivationRequest(" ", false)))
                .isInstanceOf(ValidationException.class);

        verify(userRepository, never()).findByUsername(any());
    }
}
