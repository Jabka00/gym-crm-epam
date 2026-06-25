package com.epam.gymcrm.storage;

import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.Training;
import com.epam.gymcrm.model.TrainingType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Objects;

final class StorageCsvSeeder {

    private StorageCsvSeeder() {
    }

    static void seedTrainers(TrainerStorage storage, String resourcePath) throws IOException {
        try (var reader = openResource(resourcePath)) {
            reader.lines()
                    .skip(1)
                    .filter(line -> !line.isBlank())
                    .map(line -> line.split(","))
                    .map(StorageCsvSeeder::parseTrainer)
                    .forEach(trainer -> storage.put(trainer.getUserId(), trainer));
        }
    }

    static void seedTrainees(TraineeStorage storage, String resourcePath) throws IOException {
        try (var reader = openResource(resourcePath)) {
            reader.lines()
                    .skip(1)
                    .filter(line -> !line.isBlank())
                    .map(line -> line.split(","))
                    .map(StorageCsvSeeder::parseTrainee)
                    .forEach(trainee -> storage.put(trainee.getUserId(), trainee));
        }
    }

    static void seedTrainings(TrainingStorage storage, String resourcePath) throws IOException {
        try (var reader = openResource(resourcePath)) {
            reader.lines()
                    .skip(1)
                    .filter(line -> !line.isBlank())
                    .map(line -> line.split(","))
                    .map(StorageCsvSeeder::parseTraining)
                    .forEach(training -> storage.put(training.getTrainingId(), training));
        }
    }

    private static Trainer parseTrainer(String[] parts) {
        Trainer trainer = new Trainer();
        trainer.setUserId(Long.parseLong(parts[0].trim()));
        trainer.setFirstName(parts[1].trim());
        trainer.setLastName(parts[2].trim());
        trainer.setUsername(parts[3].trim());
        trainer.setPassword(parts[4].trim());
        trainer.setActive(Boolean.parseBoolean(parts[5].trim()));
        trainer.setSpecialization(new TrainingType(parts[6].trim()));
        return trainer;
    }

    private static Trainee parseTrainee(String[] parts) {
        Trainee trainee = new Trainee();
        trainee.setUserId(Long.parseLong(parts[0].trim()));
        trainee.setFirstName(parts[1].trim());
        trainee.setLastName(parts[2].trim());
        trainee.setUsername(parts[3].trim());
        trainee.setPassword(parts[4].trim());
        trainee.setActive(Boolean.parseBoolean(parts[5].trim()));
        trainee.setDateOfBirth(LocalDate.parse(parts[6].trim()));
        trainee.setAddress(parts[7].trim());
        return trainee;
    }

    private static Training parseTraining(String[] parts) {
        Training training = new Training();
        training.setTrainingId(Long.parseLong(parts[0].trim()));
        training.setTraineeId(Long.parseLong(parts[1].trim()));
        training.setTrainerId(Long.parseLong(parts[2].trim()));
        training.setTrainingName(parts[3].trim());
        training.setTrainingType(new TrainingType(parts[4].trim()));
        training.setTrainingDate(LocalDate.parse(parts[5].trim()));
        training.setTrainingDuration(Duration.ofMinutes(Long.parseLong(parts[6].trim())));
        return training;
    }

    private static BufferedReader openResource(String path) throws IOException {
        var inputStream = Objects.requireNonNull(
                StorageCsvSeeder.class.getClassLoader().getResourceAsStream(path),
                () -> "Classpath resource not found: " + path
        );
        return new BufferedReader(new InputStreamReader(inputStream));
    }
}
