package com.epam.gymcrm.repository;

import com.epam.gymcrm.entity.TraineeEntity;
import com.epam.gymcrm.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

class TraineeRepositoryTest {

    private TraineeRepository traineeRepository;

    @BeforeEach
    void setUp() {
        traineeRepository = new TraineeRepository();
        traineeRepository.setStorage(new HashMap<>());
    }

    @Test
    void shouldSaveAndFindTraineeById() {
        TraineeEntity trainee = TestDataFactory.createTraineeWithCredentials();
        trainee.setId(1L);

        TraineeEntity saved = traineeRepository.save(trainee);

        assertThat(saved).isEqualTo(trainee);
        assertThat(traineeRepository.findById(1L)).contains(trainee);
        assertThat(traineeRepository.existsById(1L)).isTrue();
    }

    @Test
    void shouldOverwriteExistingTraineeOnSave() {
        TraineeEntity trainee = TestDataFactory.createTraineeWithCredentials();
        trainee.setId(1L);
        traineeRepository.save(trainee);
        trainee.setAddress("Odesa");

        TraineeEntity updated = traineeRepository.save(trainee);

        assertThat(updated).isEqualTo(trainee);
        assertThat(traineeRepository.findById(1L)).contains(trainee);
    }

    @Test
    void shouldDeleteTrainee() {
        TraineeEntity trainee = TestDataFactory.createTraineeWithCredentials();
        trainee.setId(1L);
        traineeRepository.save(trainee);

        traineeRepository.delete(1L);

        assertThat(traineeRepository.findById(1L)).isEmpty();
        assertThat(traineeRepository.existsById(1L)).isFalse();
    }

    @Test
    void shouldSaveMultipleTrainees() {
        TraineeEntity first = TestDataFactory.createTraineeWithCredentials();
        first.setId(1L);
        TraineeEntity second = TestDataFactory.createDefaultTrainee();
        second.setId(2L);

        traineeRepository.save(first);
        traineeRepository.save(second);

        assertThat(traineeRepository.findAll().toList())
                .containsExactlyInAnyOrder(first, second);
    }

    @Test
    void shouldReturnEmptyWhenTraineeNotFound() {
        assertThat(traineeRepository.findById(404L)).isEmpty();
    }
}
