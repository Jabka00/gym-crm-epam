package com.epam.gymcrm.repository;

import com.epam.gymcrm.entity.TrainerEntity;
import com.epam.gymcrm.support.MySqlIntegrationTest;
import com.epam.gymcrm.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@MySqlIntegrationTest
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
