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
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

final class StorageCsvSeeder {

    private StorageCsvSeeder() {
    }

    static List<Trainer> readTrainers(String resourcePath) throws IOException {
        return readLines(resourcePath, StorageCsvSeeder::parseTrainer);
    }

    static List<Trainee> readTrainees(String resourcePath) throws IOException {
        return readLines(resourcePath, StorageCsvSeeder::parseTrainee);
    }

    static List<Training> readTrainings(String resourcePath) throws IOException {
        return readLines(resourcePath, StorageCsvSeeder::parseTraining);
    }

    private static <T> List<T> readLines(String resourcePath, Function<String[], T> mapper) throws IOException {
        try (var reader = openResource(resourcePath)) {
            return reader.lines()
                    .skip(1)
                    .filter(line -> !line.isBlank())
                    .map(line -> line.split(","))
                    .map(mapper)
                    .toList();
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
