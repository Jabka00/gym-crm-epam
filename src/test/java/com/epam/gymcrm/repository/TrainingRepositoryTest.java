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
