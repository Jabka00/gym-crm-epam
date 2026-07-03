package com.epam.gymcrm.repository;

import com.epam.gymcrm.entity.TrainingEntity;
import com.epam.gymcrm.support.MySqlIntegrationTest;
import com.epam.gymcrm.support.TestDataFactory;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@MySqlIntegrationTest
class TrainingRepositoryTest {

    @Autowired
    private TrainingRepository trainingRepository;

    @Autowired
    private SessionFactory sessionFactory;

    @Test
    void shouldSaveAndFindTrainingById() {
        TrainingEntity input = TestDataFactory.createDefaultTraining(4L, 1L);

        TrainingEntity saved = trainingRepository.save(input);
        TrainingEntity expectedAfterSave = TestDataFactory.createDefaultTraining(4L, 1L);
        expectedAfterSave.setId(saved.getId());
        TrainingEntity expectedFromDb = TestDataFactory.trainingWithSeedAssociations(saved.getId());

        assertThat(saved).usingRecursiveComparison()
                .isEqualTo(expectedAfterSave);

        sessionFactory.getCurrentSession().clear();

        assertThat(trainingRepository.findById(saved.getId()))
                .get()
                .usingRecursiveComparison()
                .ignoringFields("trainee.trainers", "trainee.trainings", "trainer.trainees", "trainer.trainings")
                .isEqualTo(expectedFromDb);
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
