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

        assertThat(saved).isEqualTo(training);
        assertThat(trainingRepository.findById(1L)).contains(training);
    }

    @Test
    void shouldFindAllTrainings() {
        TrainingEntity t1 = TestDataFactory.createDefaultTraining(1L, 2L);
        t1.setTrainingId(1L);
        TrainingEntity t2 = TestDataFactory.createDefaultTraining(3L, 4L);
        t2.setTrainingId(2L);

        trainingRepository.save(t1);
        trainingRepository.save(t2);

        assertThat(trainingRepository.findAll().toList())
                .containsExactlyInAnyOrder(t1, t2);
    }

    @Test
    void shouldReturnEmptyWhenTrainingNotFound() {
        assertThat(trainingRepository.findById(404L)).isEmpty();
    }
}
