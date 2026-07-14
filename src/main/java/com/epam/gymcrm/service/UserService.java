package com.epam.gymcrm.service;

import com.epam.gymcrm.dto.request.ChangePasswordRequest;
import com.epam.gymcrm.dto.request.ToggleActivationRequest;
import com.epam.gymcrm.entity.UserEntity;
import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.repository.UserRepository;
import com.epam.gymcrm.util.DtoValidator;
import com.epam.gymcrm.util.PasswordValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordValidator passwordValidator;
    private final AuthenticationService authenticationService;
    private final DtoValidator dtoValidator;

    public void changePassword(ChangePasswordRequest request) {
        dtoValidator.validate(request);
        passwordValidator.validate(request.newPassword());

        if (!authenticationService.authenticate(request.username(), request.oldPassword()).isSuccess()) {
            throw new AuthenticationException("Invalid credentials");
        }

        UserEntity user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new EntityNotFoundException(
                        "User not found with username: " + request.username()));
        user.setPassword(request.newPassword());
        userRepository.save(user);
        log.info("Password changed");
    }

    public void toggleActivation(ToggleActivationRequest request) {
        dtoValidator.validate(request);

        UserEntity user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new EntityNotFoundException(
                        "User not found with username: " + request.username()));
        user.setActive(!user.isActive());
        userRepository.save(user);
        log.info("Toggled activation, active={}", user.isActive());
    }
}
