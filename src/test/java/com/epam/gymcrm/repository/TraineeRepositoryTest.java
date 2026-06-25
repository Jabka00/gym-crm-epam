package com.epam.gymcrm.repository;

import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TraineeRepositoryTest {

    private TraineeRepository traineeRepository;

    @BeforeEach
    void setUp() {
        traineeRepository = new TraineeRepository();
    }

    @Test
    void shouldSaveAndFindTraineeById() {
        Trainee trainee = TestDataFactory.createTraineeWithCredentials();

        Trainee saved = traineeRepository.save(trainee);

        assertThat(saved.getUserId()).isEqualTo(1L);
        assertThat(traineeRepository.findById(1L)).contains(saved);
        assertThat(traineeRepository.existsById(1L)).isTrue();
    }

    @Test
    void shouldUpdateTrainee() {
        Trainee trainee = traineeRepository.save(TestDataFactory.createTraineeWithCredentials());
        trainee.setAddress("Odesa");

        Trainee updated = traineeRepository.update(trainee);

        assertThat(updated.getAddress()).isEqualTo("Odesa");
        assertThat(traineeRepository.findById(trainee.getUserId()))
                .map(Trainee::getAddress)
                .contains("Odesa");
    }

    @Test
    void shouldDeleteTrainee() {
        Trainee trainee = traineeRepository.save(TestDataFactory.createTraineeWithCredentials());

        traineeRepository.delete(trainee.getUserId());

        assertThat(traineeRepository.findById(trainee.getUserId())).isEmpty();
        assertThat(traineeRepository.existsById(trainee.getUserId())).isFalse();
    }

    @Test
    void shouldAssignIncrementalIds() {
        Trainee first = traineeRepository.save(TestDataFactory.createTraineeWithCredentials());
        Trainee second = traineeRepository.save(TestDataFactory.createDefaultTrainee());

        assertThat(first.getUserId()).isEqualTo(1L);
        assertThat(second.getUserId()).isEqualTo(2L);
        assertThat(traineeRepository.findAll()).hasSize(2);
    }

    @Test
    void shouldReturnEmptyWhenTraineeNotFound() {
        assertThat(traineeRepository.findById(404L)).isEmpty();
    }
}
