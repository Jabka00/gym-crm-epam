package com.epam.gymcrm.service;

import com.epam.gymcrm.entity.TrainerEntity;
import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.exception.ValidationException;
import com.epam.gymcrm.repository.TraineeRepository;
import com.epam.gymcrm.repository.TrainerRepository;
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

    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final PasswordValidator passwordValidator;
    private final AuthenticationService authenticationService;

    public void changePassword(String username, String oldPassword, String newPassword) {
        validatePasswordChangeInput(username, oldPassword, newPassword);
        passwordValidator.validate(newPassword);

        if (!authenticationService.authenticate(username, oldPassword).isSuccess()) {
            throw new AuthenticationException("Invalid credentials");
        }

        traineeRepository.findByUsername(username).ifPresentOrElse(
                trainee -> {
                    trainee.getUser().setPassword(newPassword);
                    traineeRepository.save(trainee);
                    log.info("Password changed for trainee");
                },
                () -> {
                    TrainerEntity trainer = trainerRepository.findByUsername(username)
                            .orElseThrow(() -> new EntityNotFoundException(
                                    "User not found with username: " + username));
                    trainer.getUser().setPassword(newPassword);
                    trainerRepository.save(trainer);
                    log.info("Password changed for trainer");
                }
        );
    }

    public void toggleActivation(String username) {
        validateUsername(username);

        traineeRepository.findByUsername(username).ifPresentOrElse(
                trainee -> {
                    trainee.getUser().setActive(!trainee.getUser().isActive());
                    traineeRepository.save(trainee);
                    log.info("Toggled activation for trainee, active={}", trainee.getUser().isActive());
                },
                () -> {
                    TrainerEntity trainer = trainerRepository.findByUsername(username)
                            .orElseThrow(() -> new EntityNotFoundException(
                                    "User not found with username: " + username));
                    trainer.getUser().setActive(!trainer.getUser().isActive());
                    trainerRepository.save(trainer);
                    log.info("Toggled activation for trainer, active={}", trainer.getUser().isActive());
                }
        );
    }

    private void validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new ValidationException("Username cannot be null or empty");
        }
    }

    private void validatePasswordChangeInput(String username, String oldPassword, String newPassword) {
        validateUsername(username);
        if (oldPassword == null || oldPassword.isBlank()) {
            throw new ValidationException("Old password cannot be null or empty");
        }
        if (newPassword == null || newPassword.isBlank()) {
            throw new ValidationException("New password cannot be null or empty");
        }
    }
}
