package com.epam.gymcrm.service;

import com.epam.gymcrm.dto.request.ChangePasswordRequest;
import com.epam.gymcrm.dto.request.ToggleActivationRequest;
import com.epam.gymcrm.entity.UserEntity;
import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.repository.UserRepository;
import com.epam.gymcrm.util.DtoValidator;
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
    private final DtoValidator dtoValidator;

    public void changePassword(ChangePasswordRequest request) {
        dtoValidator.validate(request);

        UserEntity user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (!request.oldPassword().equals(user.getPassword())) {
            throw new AuthenticationException("Invalid credentials");
        }

        user.setPassword(request.newPassword());
        userRepository.save(user);
        log.info("Password changed for user id={}", user.getId());
    }

    public void toggleActivation(ToggleActivationRequest request) {
        dtoValidator.validate(request);

        UserEntity user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        user.setActive(!user.isActive());
        userRepository.save(user);
        log.info("Toggled activation for user id={}, active={}", user.getId(), user.isActive());
    }
}
