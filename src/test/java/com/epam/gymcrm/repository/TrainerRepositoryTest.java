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
        trainer.setUserId(1L);

        TrainerEntity saved = trainerRepository.save(trainer);

        assertThat(saved.getUserId()).isEqualTo(1L);
        assertThat(trainerRepository.findById(1L)).contains(saved);
        assertThat(trainerRepository.existsById(1L)).isTrue();
    }

    @Test
    void shouldUpdateTrainer() {
        TrainerEntity trainer = TestDataFactory.createTrainerWithCredentials();
        trainer.setUserId(1L);
        trainerRepository.save(trainer);
        trainer.setFirstName("Jonathan");

        TrainerEntity updated = trainerRepository.update(trainer);

        assertThat(updated.getFirstName()).isEqualTo("Jonathan");
        assertThat(trainerRepository.findById(1L))
                .map(TrainerEntity::getFirstName)
                .contains("Jonathan");
    }

    @Test
    void shouldSaveMultipleTrainers() {
        TrainerEntity first = TestDataFactory.createTrainerWithCredentials();
        first.setUserId(1L);
        TrainerEntity second = TestDataFactory.createDefaultTrainer();
        second.setUserId(2L);

        trainerRepository.save(first);
        trainerRepository.save(second);

        assertThat(trainerRepository.findAll().toList()).hasSize(2);
        assertThat(trainerRepository.findById(1L)).isPresent();
        assertThat(trainerRepository.findById(2L)).isPresent();
    }

    @Test
    void shouldReturnEmptyWhenTrainerNotFound() {
        assertThat(trainerRepository.findById(404L)).isEmpty();
    }
}
