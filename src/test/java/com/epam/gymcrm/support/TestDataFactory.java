package com.epam.gymcrm.support;

import com.epam.gymcrm.entity.TraineeEntity;
import com.epam.gymcrm.entity.TrainerEntity;
import com.epam.gymcrm.entity.TrainingEntity;
import com.epam.gymcrm.entity.TrainingTypeEntity;
import com.epam.gymcrm.model.TrainingType;

import java.time.LocalDate;

public final class TestDataFactory {

    private TestDataFactory() {
    }

    public static TrainerEntity createDefaultTrainer() {
        TrainerEntity trainer = new TrainerEntity();
        trainer.setFirstName("John");
        trainer.setLastName("Smith");
        trainer.setSpecialization(TrainingTypeEntity.of(TrainingType.YOGA));
        return trainer;
    }

    public static TrainerEntity createTrainerWithCredentials() {
        TrainerEntity trainer = createDefaultTrainer();
        trainer.setUsername("John.Smith");
        trainer.setPassword("secret1234");
        trainer.setActive(true);
        return trainer;
    }

    public static TraineeEntity createDefaultTrainee() {
        TraineeEntity trainee = new TraineeEntity();
        trainee.setFirstName("Alice");
        trainee.setLastName("Walker");
        trainee.setDateOfBirth(LocalDate.of(1995, 4, 12));
        trainee.setAddress("Kyiv");
        return trainee;
    }

    public static TraineeEntity createTraineeWithCredentials() {
        TraineeEntity trainee = createDefaultTrainee();
        trainee.setUsername("Alice.Walker");
        trainee.setPassword("secret1234");
        trainee.setActive(true);
        return trainee;
    }

    public static TrainingEntity createDefaultTraining(Long traineeId, Long trainerId) {
        TrainingEntity training = new TrainingEntity();

        TraineeEntity trainee = new TraineeEntity();
        trainee.setId(traineeId);
        training.setTrainee(trainee);

        TrainerEntity trainer = new TrainerEntity();
        trainer.setId(trainerId);
        training.setTrainer(trainer);

        training.setTrainingName("Morning Yoga");
        training.setTrainingType(TrainingTypeEntity.of(TrainingType.YOGA));
        training.setTrainingDate(LocalDate.of(2024, 3, 1));
        training.setTrainingDuration(60);
        return training;
    }
}
