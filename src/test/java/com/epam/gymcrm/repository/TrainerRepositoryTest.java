package com.epam.gymcrm.repository;

import com.epam.gymcrm.config.AppConfig;
import com.epam.gymcrm.entity.TrainerEntity;
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
        "db.url=jdbc:h2:mem:trainer_repository_test;DB_CLOSE_DELAY=-1",
        "db.username=sa",
        "db.password=",
        "db.driver=org.h2.Driver",
        "hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "db.init.enabled=true"
})
@Transactional
class TrainerRepositoryTest {

    @Autowired
    private TrainerRepository trainerRepository;

    @Test
    void shouldSaveAndFindTrainerById() {
        TrainerEntity input = TestDataFactory.trainer("Trainer.One");

        TrainerEntity saved = trainerRepository.save(input);
        TrainerEntity expected = TestDataFactory.trainer("Trainer.One");
        expected.setId(saved.getId());

        assertThat(saved).usingRecursiveComparison()
                .ignoringFields("trainees", "trainings")
                .isEqualTo(expected);
        assertThat(trainerRepository.findById(saved.getId()))
                .get()
                .usingRecursiveComparison()
                .ignoringFields("trainees", "trainings")
                .isEqualTo(expected);
    }

    @Test
    void shouldOverwriteExistingTrainerOnSave() {
        TrainerEntity saved = trainerRepository.save(TestDataFactory.trainer("Trainer.Two"));
        saved.setFirstName("Jonathan");

        TrainerEntity updated = trainerRepository.save(saved);
        TrainerEntity expected = TestDataFactory.trainer("Trainer.Two");
        expected.setId(saved.getId());
        expected.setFirstName("Jonathan");

        assertThat(updated).usingRecursiveComparison()
                .ignoringFields("trainees", "trainings")
                .isEqualTo(expected);
        assertThat(trainerRepository.findById(saved.getId()))
                .get()
                .usingRecursiveComparison()
                .ignoringFields("trainees", "trainings")
                .isEqualTo(expected);
    }

    @Test
    void shouldReturnEmptyWhenTrainerNotFound() {
        assertThat(trainerRepository.findById(404L)).isEmpty();
    }
}
