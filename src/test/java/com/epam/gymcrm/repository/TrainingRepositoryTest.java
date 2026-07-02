package com.epam.gymcrm.repository;

import com.epam.gymcrm.config.AppConfig;
import com.epam.gymcrm.entity.TrainingEntity;
import com.epam.gymcrm.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = AppConfig.class)
@TestPropertySource(properties = {
        "db.url=jdbc:h2:mem:training_repository_test;DB_CLOSE_DELAY=-1",
        "db.username=sa",
        "db.password=",
        "db.driver=org.h2.Driver",
        "hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "db.init.enabled=true"
})
@Transactional
class TrainingRepositoryTest {

    @Autowired
    private TrainingRepository trainingRepository;

    @Test
    void shouldSaveAndFindTrainingById() {
        TrainingEntity training = trainingWithId(100L);

        TrainingEntity saved = trainingRepository.save(training);

        assertThat(saved.getId()).isEqualTo(100L);
        assertThat(trainingRepository.findById(100L)).isPresent();
    }

    @Test
    void shouldFindAllTrainings() {
        TrainingEntity t1 = trainingWithId(101L);
        TrainingEntity t2 = trainingWithId(102L);
        t2.setTrainingName("Evening CrossFit");
        t2.getTrainee().setId(5L);

        trainingRepository.save(t1);
        trainingRepository.save(t2);

        assertThat(trainingRepository.findAll().map(TrainingEntity::getId).toList())
                .contains(101L, 102L);
    }

    @Test
    void shouldDeleteTraining() {
        TrainingEntity training = trainingWithId(103L);
        trainingRepository.save(training);

        trainingRepository.delete(103L);

        assertThat(trainingRepository.findById(103L)).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenTrainingNotFound() {
        assertThat(trainingRepository.findById(404L)).isEmpty();
    }

    private static TrainingEntity trainingWithId(long id) {
        TrainingEntity training = TestDataFactory.createDefaultTraining(4L, 1L);
        training.setId(id);
        training.getTrainingType().setId(1L);
        return training;
    }
}
