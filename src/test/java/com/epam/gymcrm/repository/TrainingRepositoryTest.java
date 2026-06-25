package com.epam.gymcrm.repository;

import com.epam.gymcrm.model.Training;
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
        Training training = TestDataFactory.createDefaultTraining(1L, 2L);

        Training saved = trainingRepository.save(training);

        assertThat(saved.getTrainingId()).isEqualTo(1L);
        assertThat(trainingRepository.findById(1L)).contains(saved);
    }

    @Test
    void shouldFindAllTrainings() {
        trainingRepository.save(TestDataFactory.createDefaultTraining(1L, 2L));
        trainingRepository.save(TestDataFactory.createDefaultTraining(3L, 4L));

        assertThat(trainingRepository.findAll()).hasSize(2);
    }

    @Test
    void shouldAssignIncrementalIds() {
        Training first = trainingRepository.save(TestDataFactory.createDefaultTraining(1L, 2L));
        Training second = trainingRepository.save(TestDataFactory.createDefaultTraining(3L, 4L));

        assertThat(first.getTrainingId()).isEqualTo(1L);
        assertThat(second.getTrainingId()).isEqualTo(2L);
    }

    @Test
    void shouldReturnEmptyWhenTrainingNotFound() {
        assertThat(trainingRepository.findById(404L)).isEmpty();
    }
}
