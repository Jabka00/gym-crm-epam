package com.epam.gymcrm.repository;

import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TrainerRepositoryTest {

    private TrainerRepository trainerRepository;

    @BeforeEach
    void setUp() {
        trainerRepository = new TrainerRepository();
    }

    @Test
    void shouldSaveAndFindTrainerById() {
        Trainer trainer = TestDataFactory.createTrainerWithCredentials();

        Trainer saved = trainerRepository.save(trainer);

        assertThat(saved.getUserId()).isEqualTo(1L);
        assertThat(trainerRepository.findById(1L)).contains(saved);
        assertThat(trainerRepository.existsById(1L)).isTrue();
    }

    @Test
    void shouldUpdateTrainer() {
        Trainer trainer = trainerRepository.save(TestDataFactory.createTrainerWithCredentials());
        trainer.setFirstName("Jonathan");

        Trainer updated = trainerRepository.update(trainer);

        assertThat(updated.getFirstName()).isEqualTo("Jonathan");
        assertThat(trainerRepository.findById(trainer.getUserId()))
                .map(Trainer::getFirstName)
                .contains("Jonathan");
    }

    @Test
    void shouldAssignIncrementalIds() {
        Trainer first = trainerRepository.save(TestDataFactory.createTrainerWithCredentials());
        Trainer second = trainerRepository.save(TestDataFactory.createDefaultTrainer());

        assertThat(first.getUserId()).isEqualTo(1L);
        assertThat(second.getUserId()).isEqualTo(2L);
        assertThat(trainerRepository.findAll()).hasSize(2);
    }

    @Test
    void shouldReturnEmptyWhenTrainerNotFound() {
        assertThat(trainerRepository.findById(404L)).isEmpty();
    }
}
