package com.epam.gymcrm.repository;

import com.epam.gymcrm.entity.TraineeEntity;
import com.epam.gymcrm.support.MySqlIntegrationTest;
import com.epam.gymcrm.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@MySqlIntegrationTest
class TraineeRepositoryTest {

    @Autowired
    private TraineeRepository traineeRepository;

    @Test
    void shouldSaveAndFindTraineeById() {
        TraineeEntity input = TestDataFactory.trainee("First.User");

        TraineeEntity saved = traineeRepository.save(input);
        TraineeEntity expected = TestDataFactory.trainee("First.User");
        expected.setId(saved.getId());

        assertThat(saved).usingRecursiveComparison()
                .ignoringFields("trainers", "trainings")
                .isEqualTo(expected);
        assertThat(traineeRepository.findById(saved.getId()))
                .get()
                .usingRecursiveComparison()
                .ignoringFields("trainers", "trainings")
                .isEqualTo(expected);
    }

    @Test
    void shouldOverwriteExistingTraineeOnSave() {
        TraineeEntity saved = traineeRepository.save(TestDataFactory.trainee("Second.User"));
        saved.setAddress("Odesa");

        TraineeEntity updated = traineeRepository.save(saved);
        TraineeEntity expected = TestDataFactory.trainee("Second.User");
        expected.setId(saved.getId());
        expected.setAddress("Odesa");

        assertThat(updated).usingRecursiveComparison()
                .ignoringFields("trainers", "trainings")
                .isEqualTo(expected);
        assertThat(traineeRepository.findById(saved.getId()))
                .get()
                .usingRecursiveComparison()
                .ignoringFields("trainers", "trainings")
                .isEqualTo(expected);
    }

    @Test
    void shouldDeleteTrainee() {
        TraineeEntity saved = traineeRepository.save(TestDataFactory.trainee("Third.User"));

        traineeRepository.delete(saved.getId());

        assertThat(traineeRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenTraineeNotFound() {
        assertThat(traineeRepository.findById(404L)).isEmpty();
    }
}
