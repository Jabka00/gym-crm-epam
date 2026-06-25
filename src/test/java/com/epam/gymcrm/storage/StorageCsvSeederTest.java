package com.epam.gymcrm.storage;

import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.Training;
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
        List<Trainer> trainers = StorageCsvSeeder.readTrainers("data/trainers.csv");

        assertThat(trainers).hasSize(3);

        Trainer first = trainers.get(0);
        assertThat(first.getUserId()).isEqualTo(1L);
        assertThat(first.getFirstName()).isEqualTo("John");
        assertThat(first.getLastName()).isEqualTo("Smith");
        assertThat(first.getUsername()).isEqualTo("John.Smith");
        assertThat(first.isActive()).isTrue();
        assertThat(first.getSpecialization().trainingTypeName()).isEqualTo("Yoga");

        assertThat(trainers.get(2).isActive()).isFalse();
    }

    @Test
    void shouldReadTraineesFromCsv() throws IOException {
        List<Trainee> trainees = StorageCsvSeeder.readTrainees("data/trainees.csv");

        assertThat(trainees).hasSize(3);

        Trainee first = trainees.get(0);
        assertThat(first.getUserId()).isEqualTo(1L);
        assertThat(first.getFirstName()).isEqualTo("Alice");
        assertThat(first.getLastName()).isEqualTo("Walker");
        assertThat(first.getUsername()).isEqualTo("Alice.Walker");
        assertThat(first.isActive()).isTrue();
        assertThat(first.getDateOfBirth()).isEqualTo(LocalDate.of(1995, 4, 12));
        assertThat(first.getAddress()).isEqualTo("123 Main St");

        assertThat(trainees.get(2).isActive()).isFalse();
    }

    @Test
    void shouldReadTrainingsFromCsv() throws IOException {
        List<Training> trainings = StorageCsvSeeder.readTrainings("data/trainings.csv");

        assertThat(trainings).hasSize(3);

        Training first = trainings.get(0);
        assertThat(first.getTrainingId()).isEqualTo(1L);
        assertThat(first.getTraineeId()).isEqualTo(1L);
        assertThat(first.getTrainerId()).isEqualTo(1L);
        assertThat(first.getTrainingName()).isEqualTo("Morning Yoga");
        assertThat(first.getTrainingType().trainingTypeName()).isEqualTo("Yoga");
        assertThat(first.getTrainingDate()).isEqualTo(LocalDate.of(2024, 3, 1));
        assertThat(first.getTrainingDuration()).isEqualTo(Duration.ofMinutes(60));
    }

    @Test
    void shouldThrowWhenResourceNotFound() {
        assertThatThrownBy(() -> StorageCsvSeeder.readTrainers("data/missing.csv"))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("data/missing.csv");
    }
}
