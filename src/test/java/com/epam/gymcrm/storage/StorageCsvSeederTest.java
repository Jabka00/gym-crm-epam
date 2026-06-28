package com.epam.gymcrm.storage;

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

class StorageCsvSeederTest {

    @Test
    void shouldReadTrainersFromCsv() throws IOException {
        List<TrainerEntity> trainers = StorageCsvSeeder.readTrainers("data/trainers.csv");

        assertThat(trainers).hasSize(3);

        TrainerEntity expected = new TrainerEntity();
        expected.setUserId(1L);
        expected.setFirstName("John");
        expected.setLastName("Smith");
        expected.setUsername("John.Smith");
        expected.setPassword("pass1234AB");
        expected.setActive(true);
        expected.setSpecialization(TrainingType.YOGA);

        assertThat(trainers.get(0)).isEqualTo(expected);
        assertThat(trainers.get(2).isActive()).isFalse();
    }

    @Test
    void shouldReadTraineesFromCsv() throws IOException {
        List<TraineeEntity> trainees = StorageCsvSeeder.readTrainees("data/trainees.csv");

        assertThat(trainees).hasSize(3);

        TraineeEntity expected = new TraineeEntity();
        expected.setUserId(1L);
        expected.setFirstName("Alice");
        expected.setLastName("Walker");
        expected.setUsername("Alice.Walker");
        expected.setPassword("qW3eRt5yUi");
        expected.setActive(true);
        expected.setDateOfBirth(LocalDate.of(1995, 4, 12));
        expected.setAddress("123 Main St");

        assertThat(trainees.get(0)).isEqualTo(expected);
        assertThat(trainees.get(2).isActive()).isFalse();
    }

    @Test
    void shouldReadTrainingsFromCsv() throws IOException {
        List<TrainingEntity> trainings = StorageCsvSeeder.readTrainings("data/trainings.csv");

        assertThat(trainings).hasSize(3);

        TrainingEntity expected = new TrainingEntity();
        expected.setTrainingId(1L);
        expected.setTraineeId(1L);
        expected.setTrainerId(1L);
        expected.setTrainingName("Morning Yoga");
        expected.setTrainingType(TrainingType.YOGA);
        expected.setTrainingDate(LocalDate.of(2024, 3, 1));
        expected.setTrainingDuration(Duration.ofMinutes(60));

        assertThat(trainings.get(0)).isEqualTo(expected);
    }

    @Test
    void shouldThrowWhenResourceNotFound() {
        assertThatThrownBy(() -> StorageCsvSeeder.readTrainers("data/missing.csv"))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("data/missing.csv");
    }
}
