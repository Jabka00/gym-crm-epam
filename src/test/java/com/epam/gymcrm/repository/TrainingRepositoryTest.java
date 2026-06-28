package com.epam.gymcrm.repository;

import com.epam.gymcrm.entity.TrainingEntity;
import com.epam.gymcrm.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

class TrainingRepositoryTest {

    private TrainingRepository trainingRepository;

    @BeforeEach
    void setUp() {
        trainingRepository = new TrainingRepository();
        trainingRepository.setStorage(new HashMap<>());
    }

    @Test
    void shouldSaveAndFindTrainingById() {
        TrainingEntity training = TestDataFactory.createDefaultTraining(1L, 2L);
        training.setTrainingId(1L);

        TrainingEntity saved = trainingRepository.save(training);

        assertThat(saved.getTrainingId()).isEqualTo(1L);
        assertThat(trainingRepository.findById(1L)).contains(saved);
    }

    @Test
    void shouldFindAllTrainings() {
        TrainingEntity t1 = TestDataFactory.createDefaultTraining(1L, 2L);
        t1.setTrainingId(1L);
        TrainingEntity t2 = TestDataFactory.createDefaultTraining(3L, 4L);
        t2.setTrainingId(2L);

        trainingRepository.save(t1);
        trainingRepository.save(t2);

        assertThat(trainingRepository.findAll().toList()).hasSize(2);
    }

    @Test
    void shouldSaveMultipleTrainings() {
        TrainingEntity first = TestDataFactory.createDefaultTraining(1L, 2L);
        first.setTrainingId(1L);
        TrainingEntity second = TestDataFactory.createDefaultTraining(3L, 4L);
        second.setTrainingId(2L);

        trainingRepository.save(first);
        trainingRepository.save(second);

        assertThat(trainingRepository.findById(1L)).isPresent();
        assertThat(trainingRepository.findById(2L)).isPresent();
    }

    @Test
    void shouldReturnEmptyWhenTrainingNotFound() {
        assertThat(trainingRepository.findById(404L)).isEmpty();
    }
}
