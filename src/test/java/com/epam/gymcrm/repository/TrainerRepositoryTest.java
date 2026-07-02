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
        TrainerEntity trainer = trainerWithId(100L, "One");

        TrainerEntity saved = trainerRepository.save(trainer);

        assertThat(saved.getId()).isEqualTo(100L);
        assertThat(trainerRepository.findById(100L)).isPresent();
        assertThat(trainerRepository.existsById(100L)).isTrue();
    }

    @Test
    void shouldOverwriteExistingTrainerOnSave() {
        TrainerEntity trainer = trainerWithId(101L, "Two");
        trainerRepository.save(trainer);
        trainer.setFirstName("Jonathan");

        TrainerEntity updated = trainerRepository.save(trainer);

        assertThat(updated.getFirstName()).isEqualTo("Jonathan");
        assertThat(trainerRepository.findById(101L))
                .get()
                .extracting(TrainerEntity::getFirstName)
                .isEqualTo("Jonathan");
    }

    @Test
    void shouldSaveMultipleTrainers() {
        TrainerEntity first = trainerWithId(102L, "Three");
        TrainerEntity second = trainerWithId(103L, "Four");

        trainerRepository.save(first);
        trainerRepository.save(second);

        assertThat(trainerRepository.findAll().map(TrainerEntity::getId).toList())
                .contains(102L, 103L);
    }

    @Test
    void shouldReturnEmptyWhenTrainerNotFound() {
        assertThat(trainerRepository.findById(404L)).isEmpty();
    }

    private static TrainerEntity trainerWithId(long id, String usernameSuffix) {
        TrainerEntity trainer = TestDataFactory.createTrainerWithCredentials();
        trainer.setId(id);
        trainer.setUsername("Trainer." + usernameSuffix);
        trainer.getSpecialization().setId(1L);
        return trainer;
    }
}
