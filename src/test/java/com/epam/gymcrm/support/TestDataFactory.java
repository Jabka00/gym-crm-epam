package com.epam.gymcrm.support;

import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.Training;
import com.epam.gymcrm.model.TrainingType;

import java.time.Duration;
import java.time.LocalDate;

public final class TestDataFactory {

    private TestDataFactory() {
    }

    public static Trainer createDefaultTrainer() {
        Trainer trainer = new Trainer();
        trainer.setFirstName("John");
        trainer.setLastName("Smith");
        trainer.setSpecialization(new TrainingType("Yoga"));
        return trainer;
    }

    public static Trainer createTrainerWithCredentials() {
        Trainer trainer = createDefaultTrainer();
        trainer.setUsername("John.Smith");
        trainer.setPassword("secret1234");
        trainer.setActive(true);
        return trainer;
    }

    public static Trainee createDefaultTrainee() {
        Trainee trainee = new Trainee();
        trainee.setFirstName("Alice");
        trainee.setLastName("Walker");
        trainee.setDateOfBirth(LocalDate.of(1995, 4, 12));
        trainee.setAddress("Kyiv");
        return trainee;
    }

    public static Trainee createTraineeWithCredentials() {
        Trainee trainee = createDefaultTrainee();
        trainee.setUsername("Alice.Walker");
        trainee.setPassword("secret1234");
        trainee.setActive(true);
        return trainee;
    }

    public static Training createDefaultTraining(Long traineeId, Long trainerId) {
        Training training = new Training();
        training.setTraineeId(traineeId);
        training.setTrainerId(trainerId);
        training.setTrainingName("Morning Yoga");
        training.setTrainingType(new TrainingType("Yoga"));
        training.setTrainingDate(LocalDate.of(2024, 3, 1));
        training.setTrainingDuration(Duration.ofMinutes(60));
        return training;
    }
}
