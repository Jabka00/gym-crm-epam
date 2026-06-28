package com.epam.gymcrm.util;

import com.epam.gymcrm.entity.TraineeEntity;
import com.epam.gymcrm.entity.TrainerEntity;
import com.epam.gymcrm.entity.TrainingEntity;
import com.epam.gymcrm.entity.TrainingType;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CsvDataReaderTest {

    @Test
    void shouldReadTrainersFromCsv() throws IOException {
        List<TrainerEntity> trainers = CsvDataReader.readTrainers("data/trainers.csv");

        assertThat(trainers).containsExactly(
                trainer(1L, "John", "Smith", "John.Smith", "pass1234AB", true, TrainingType.YOGA),
                trainer(2L, "Anna", "Jones", "Anna.Jones", "xK9mPqWz1L", true, TrainingType.CROSSFIT),
                trainer(3L, "Mike", "Brown", "Mike.Brown", "Tz7nVbCx4R", false, TrainingType.BOXING)
        );
    }

    @Test
    void shouldReadTraineesFromCsv() throws IOException {
        List<TraineeEntity> trainees = CsvDataReader.readTrainees("data/trainees.csv");

        assertThat(trainees).containsExactly(
                trainee(1L, "Alice", "Walker", "Alice.Walker", "qW3eRt5yUi", true,
                        LocalDate.of(1995, 4, 12), "123 Main St"),
                trainee(2L, "Bob", "Taylor", "Bob.Taylor", "Lm6oPs8dFg", true,
                        LocalDate.of(1990, 7, 30), "456 Oak Ave"),
                trainee(3L, "Carol", "White", "Carol.White", "Hn2jKx9cVb", false,
                        LocalDate.of(2000, 1, 15), "789 Pine Rd")
        );
    }

    @Test
    void shouldReadTrainingsFromCsv() throws IOException {
        List<TrainingEntity> trainings = CsvDataReader.readTrainings("data/trainings.csv");

        assertThat(trainings).containsExactly(
                training(1L, 1L, 1L, "Morning Yoga", TrainingType.YOGA,
                        LocalDate.of(2024, 3, 1), Duration.ofMinutes(60)),
                training(2L, 2L, 2L, "CrossFit Intro", TrainingType.CROSSFIT,
                        LocalDate.of(2024, 3, 2), Duration.ofMinutes(90)),
                training(3L, 1L, 3L, "Boxing Basics", TrainingType.BOXING,
                        LocalDate.of(2024, 3, 3), Duration.ofMinutes(45))
        );
    }

    @Test
    void shouldThrowWhenResourceNotFound() {
        assertThatThrownBy(() -> CsvDataReader.readTrainers("data/missing.csv"))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("data/missing.csv");
    }

    private static TrainerEntity trainer(Long id, String firstName, String lastName,
                                         String username, String password,
                                         boolean active, TrainingType specialization) {
        TrainerEntity e = new TrainerEntity();
        e.setUserId(id);
        e.setFirstName(firstName);
        e.setLastName(lastName);
        e.setUsername(username);
        e.setPassword(password);
        e.setActive(active);
        e.setSpecialization(specialization);
        return e;
    }

    private static TraineeEntity trainee(Long id, String firstName, String lastName,
                                          String username, String password, boolean active,
                                          LocalDate dateOfBirth, String address) {
        TraineeEntity e = new TraineeEntity();
        e.setUserId(id);
        e.setFirstName(firstName);
        e.setLastName(lastName);
        e.setUsername(username);
        e.setPassword(password);
        e.setActive(active);
        e.setDateOfBirth(dateOfBirth);
        e.setAddress(address);
        return e;
    }

    private static TrainingEntity training(Long id, Long traineeId, Long trainerId,
                                            String name, TrainingType type,
                                            LocalDate date, Duration duration) {
        TrainingEntity e = new TrainingEntity();
        e.setTrainingId(id);
        e.setTraineeId(traineeId);
        e.setTrainerId(trainerId);
        e.setTrainingName(name);
        e.setTrainingType(type);
        e.setTrainingDate(date);
        e.setTrainingDuration(duration);
        return e;
    }
}
