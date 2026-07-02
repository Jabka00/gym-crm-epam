package com.epam.gymcrm.repository;

import com.epam.gymcrm.entity.TrainingEntity;
import com.epam.gymcrm.support.MySqlIntegrationTest;
import com.epam.gymcrm.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@MySqlIntegrationTest
class TrainingRepositoryTest {

    @Autowired
    private TrainingRepository trainingRepository;

    @Test
    void shouldSaveAndFindTrainingById() {
        TrainingEntity input = TestDataFactory.createDefaultTraining(4L, 1L);

        TrainingEntity saved = trainingRepository.save(input);
        TrainingEntity expected = TestDataFactory.createDefaultTraining(4L, 1L);
        expected.setId(saved.getId());

        assertThat(saved).usingRecursiveComparison()
                .ignoringFields("trainee", "trainer", "trainingType")
                .isEqualTo(expected);
        assertThat(trainingRepository.findById(saved.getId()))
                .get()
                .usingRecursiveComparison()
                .ignoringFields("trainee", "trainer", "trainingType")
                .isEqualTo(saved);
        assertThat(trainingRepository.findById(saved.getId()).get().getTrainee().getId()).isEqualTo(4L);
        assertThat(trainingRepository.findById(saved.getId()).get().getTrainer().getId()).isEqualTo(1L);
    }

    @Test
    void shouldDeleteTraining() {
        TrainingEntity saved = trainingRepository.save(TestDataFactory.createDefaultTraining(4L, 1L));

        trainingRepository.delete(saved.getId());

        assertThat(trainingRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenTrainingNotFound() {
        assertThat(trainingRepository.findById(404L)).isEmpty();
    }
}
