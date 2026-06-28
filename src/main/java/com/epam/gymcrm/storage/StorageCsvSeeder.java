package com.epam.gymcrm.storage;

import com.epam.gymcrm.entity.TraineeEntity;
import com.epam.gymcrm.entity.TrainerEntity;
import com.epam.gymcrm.entity.TrainingEntity;
import com.epam.gymcrm.entity.TrainingType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public final class StorageCsvSeeder {

    private StorageCsvSeeder() {
    }

    public static List<TrainerEntity> readTrainers(String resourcePath) throws IOException {
        return readLines(resourcePath, StorageCsvSeeder::parseTrainer);
    }

    public static List<TraineeEntity> readTrainees(String resourcePath) throws IOException {
        return readLines(resourcePath, StorageCsvSeeder::parseTrainee);
    }

    public static List<TrainingEntity> readTrainings(String resourcePath) throws IOException {
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

    private static TrainerEntity parseTrainer(String[] parts) {
        TrainerEntity trainer = new TrainerEntity();
        trainer.setUserId(Long.parseLong(parts[0].trim()));
        trainer.setFirstName(parts[1].trim());
        trainer.setLastName(parts[2].trim());
        trainer.setUsername(parts[3].trim());
        trainer.setPassword(parts[4].trim());
        trainer.setActive(Boolean.parseBoolean(parts[5].trim()));
        trainer.setSpecialization(TrainingType.valueOf(parts[6].trim().toUpperCase()));
        return trainer;
    }

    private static TraineeEntity parseTrainee(String[] parts) {
        TraineeEntity trainee = new TraineeEntity();
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

    private static TrainingEntity parseTraining(String[] parts) {
        TrainingEntity training = new TrainingEntity();
        training.setTrainingId(Long.parseLong(parts[0].trim()));
        training.setTraineeId(Long.parseLong(parts[1].trim()));
        training.setTrainerId(Long.parseLong(parts[2].trim()));
        training.setTrainingName(parts[3].trim());
        training.setTrainingType(TrainingType.valueOf(parts[4].trim().toUpperCase()));
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
