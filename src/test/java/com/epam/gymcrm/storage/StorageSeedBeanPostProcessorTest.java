package com.epam.gymcrm.storage;

import com.epam.gymcrm.repository.TraineeRepository;
import com.epam.gymcrm.repository.TrainerRepository;
import com.epam.gymcrm.repository.TrainingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StorageSeedBeanPostProcessorTest {

    @Mock
    private Environment environment;

    private StorageSeedBeanPostProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new StorageSeedBeanPostProcessor();
        processor.setEnvironment(environment);
    }

    @Test
    void shouldSeedTrainerRepository() {
        when(environment.getRequiredProperty("storage.data.trainers")).thenReturn("data/trainers.csv");
        TrainerRepository repository = new TrainerRepository();

        Object result = processor.postProcessAfterInitialization(repository, "trainerRepository");

        assertThat(result).isSameAs(repository);
        assertThat(repository.findAll()).hasSize(3);
        assertThat(repository.findById(1L)).isPresent();
    }

    @Test
    void shouldSeedTraineeRepository() {
        when(environment.getRequiredProperty("storage.data.trainees")).thenReturn("data/trainees.csv");
        TraineeRepository repository = new TraineeRepository();

        Object result = processor.postProcessAfterInitialization(repository, "traineeRepository");

        assertThat(result).isSameAs(repository);
        assertThat(repository.findAll()).hasSize(3);
        assertThat(repository.findById(1L)).isPresent();
    }

    @Test
    void shouldSeedTrainingRepository() {
        when(environment.getRequiredProperty("storage.data.trainings")).thenReturn("data/trainings.csv");
        TrainingRepository repository = new TrainingRepository();

        Object result = processor.postProcessAfterInitialization(repository, "trainingRepository");

        assertThat(result).isSameAs(repository);
        assertThat(repository.findAll()).hasSize(3);
        assertThat(repository.findById(1L)).isPresent();
    }

    @Test
    void shouldReturnNonRepositoryBeanUnchanged() {
        Object bean = new Object();

        Object result = processor.postProcessAfterInitialization(bean, "someOtherBean");

        assertThat(result).isSameAs(bean);
    }
}
