package com.epam.gymcrm.storage;

import com.epam.gymcrm.config.StorageSeedBeanPostProcessor;
import com.epam.gymcrm.repository.TraineeRepository;
import com.epam.gymcrm.repository.TrainerRepository;
import com.epam.gymcrm.repository.TrainingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StorageSeedBeanPostProcessorTest {

    private StorageSeedBeanPostProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new StorageSeedBeanPostProcessor();
        processor.setTrainersResource("data/trainers.csv");
        processor.setTraineesResource("data/trainees.csv");
        processor.setTrainingsResource("data/trainings.csv");
    }

    @Test
    void shouldSeedTrainerRepository() {
        TrainerRepository repository = new TrainerRepository();

        Object result = processor.postProcessAfterInitialization(repository, "trainerRepository");

        assertThat(result).isSameAs(repository);
        assertThat(repository.findAll().toList()).hasSize(3);
        assertThat(repository.findById(1L)).isPresent();
    }

    @Test
    void shouldSeedTraineeRepository() {
        TraineeRepository repository = new TraineeRepository();

        Object result = processor.postProcessAfterInitialization(repository, "traineeRepository");

        assertThat(result).isSameAs(repository);
        assertThat(repository.findAll().toList()).hasSize(3);
        assertThat(repository.findById(1L)).isPresent();
    }

    @Test
    void shouldSeedTrainingRepository() {
        TrainingRepository repository = new TrainingRepository();

        Object result = processor.postProcessAfterInitialization(repository, "trainingRepository");

        assertThat(result).isSameAs(repository);
        assertThat(repository.findAll().toList()).hasSize(3);
        assertThat(repository.findById(1L)).isPresent();
    }

    @Test
    void shouldReturnNonRepositoryBeanUnchanged() {
        Object bean = new Object();

        Object result = processor.postProcessAfterInitialization(bean, "someOtherBean");

        assertThat(result).isSameAs(bean);
    }
}
