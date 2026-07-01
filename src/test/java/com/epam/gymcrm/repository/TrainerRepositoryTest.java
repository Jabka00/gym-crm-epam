package com.epam.gymcrm.repository;

import com.epam.gymcrm.entity.TrainerEntity;
import com.epam.gymcrm.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

class TrainerRepositoryTest {

    private TrainerRepository trainerRepository;

    @BeforeEach
    void setUp() {
        trainerRepository = new TrainerRepository();
        trainerRepository.setStorage(new HashMap<>());
    }

    @Test
    void shouldSaveAndFindTrainerById() {
        TrainerEntity trainer = TestDataFactory.createTrainerWithCredentials();
        trainer.setId(1L);

        TrainerEntity saved = trainerRepository.save(trainer);

        assertThat(saved).isEqualTo(trainer);
        assertThat(trainerRepository.findById(1L)).contains(trainer);
        assertThat(trainerRepository.existsById(1L)).isTrue();
    }

    @Test
    void shouldOverwriteExistingTrainerOnSave() {
        TrainerEntity trainer = TestDataFactory.createTrainerWithCredentials();
        trainer.setId(1L);
        trainerRepository.save(trainer);
        trainer.setFirstName("Jonathan");

        TrainerEntity updated = trainerRepository.save(trainer);

        assertThat(updated).isEqualTo(trainer);
        assertThat(trainerRepository.findById(1L)).contains(trainer);
    }

    @Test
    void shouldSaveMultipleTrainers() {
        TrainerEntity first = TestDataFactory.createTrainerWithCredentials();
        first.setId(1L);
        TrainerEntity second = TestDataFactory.createDefaultTrainer();
        second.setId(2L);

        trainerRepository.save(first);
        trainerRepository.save(second);

        assertThat(trainerRepository.findAll().toList())
                .containsExactlyInAnyOrder(first, second);
    }

    @Test
    void shouldReturnEmptyWhenTrainerNotFound() {
        assertThat(trainerRepository.findById(404L)).isEmpty();
    }
}
