package com.epam.gymcrm.util;

import com.epam.gymcrm.entity.TraineeEntity;
import com.epam.gymcrm.entity.TrainerEntity;
import com.epam.gymcrm.entity.UserEntity;
import com.epam.gymcrm.service.UserCredentialService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.UnaryOperator;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserInitializationUtil {

    private final UserCredentialService userCredentialService;

    public TraineeEntity createTrainee(TraineeEntity trainee, UnaryOperator<TraineeEntity> saver) {
        return initializeAndSave(trainee, saver, "Trainee");
    }

    public TrainerEntity createTrainer(TrainerEntity trainer, UnaryOperator<TrainerEntity> saver) {
        return initializeAndSave(trainer, saver, "Trainer");
    }

    private <T extends UserEntity> T initializeAndSave(T user, UnaryOperator<T> saver, String userType) {
        String username = userCredentialService.generateUniqueUsername(
                user.getFirstName(),
                user.getLastName()
        );
        String password = userCredentialService.generatePassword();

        user.setUsername(username);
        user.setPassword(password);
        user.setActive(true);

        T savedUser = saver.apply(user);
        log.info("{} profile created successfully with ID: {}", userType, savedUser.getId());
        return savedUser;
    }
}
