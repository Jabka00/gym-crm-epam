package com.epam.gymcrm.service;

import com.epam.gymcrm.entity.TraineeEntity;
import com.epam.gymcrm.entity.TrainerEntity;
import com.epam.gymcrm.entity.UserEntity;
import com.epam.gymcrm.exception.EntityNotFoundException;
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

    public void changePassword(String username, String oldPassword, String newPassword) {
        validatePasswordChangeInput(username, oldPassword, newPassword);
        passwordValidator.validate(newPassword);

        var traineeOpt = traineeRepository.findByUsername(username);
        if (traineeOpt.isPresent()) {
            TraineeEntity trainee = traineeOpt.get();
            validateOldPassword(trainee, oldPassword);
            trainee.setPassword(newPassword);
            traineeRepository.save(trainee);
            return;
        }

        var trainerOpt = trainerRepository.findByUsername(username);
        if (trainerOpt.isPresent()) {
            TrainerEntity trainer = trainerOpt.get();
            validateOldPassword(trainer, oldPassword);
            trainer.setPassword(newPassword);
            trainerRepository.save(trainer);
            return;
        }

        throw new EntityNotFoundException("User not found with username: " + username);
    }

    public void toggleActivation(String username) {
        validateUsername(username);

        var traineeOpt = traineeRepository.findByUsername(username);
        if (traineeOpt.isPresent()) {
            TraineeEntity trainee = traineeOpt.get();
            trainee.setActive(!trainee.isActive());
            traineeRepository.save(trainee);
            return;
        }

        var trainerOpt = trainerRepository.findByUsername(username);
        if (trainerOpt.isPresent()) {
            TrainerEntity trainer = trainerOpt.get();
            trainer.setActive(!trainer.isActive());
            trainerRepository.save(trainer);
            return;
        }

        throw new EntityNotFoundException("User not found with username: " + username);
    }

    public void deleteByUsername(String username) {
        validateUsername(username);

        var traineeOpt = traineeRepository.findByUsername(username);
        if (traineeOpt.isPresent()) {
            traineeRepository.delete(traineeOpt.get().getId());
            return;
        }

        throw new EntityNotFoundException("User not found with username: " + username);
    }

    private <T extends UserEntity> void validateOldPassword(T user, String oldPassword) {
        if (!user.getPassword().equals(oldPassword)) {
            throw new IllegalArgumentException("Old password is incorrect");
        }
    }

    private void validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
    }

    private void validatePasswordChangeInput(String username, String oldPassword, String newPassword) {
        validateUsername(username);
        if (oldPassword == null || oldPassword.isBlank()) {
            throw new IllegalArgumentException("Old password cannot be null or empty");
        }
        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("New password cannot be null or empty");
        }
    }
}
