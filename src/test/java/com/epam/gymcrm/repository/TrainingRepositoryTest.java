package com.epam.gymcrm.repository;

import com.epam.gymcrm.model.TrainingType;

import com.epam.gymcrm.entity.TrainingEntity;
import com.epam.gymcrm.support.MySqlIntegrationTest;
import com.epam.gymcrm.support.TestDataFactory;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@MySqlIntegrationTest
class TrainingRepositoryTest {

    @Autowired
    private TrainingRepository trainingRepository;

    @Autowired
    private SessionFactory sessionFactory;

    @Test
    void shouldSaveAndFindTrainingById() {
        TrainingEntity input = TestDataFactory.createDefaultTraining(4L, 1L);

        TrainingEntity saved = trainingRepository.save(input);
        TrainingEntity expectedAfterSave = TestDataFactory.createDefaultTraining(4L, 1L);
        expectedAfterSave.setId(saved.getId());
        TrainingEntity expectedFromDb = TestDataFactory.trainingWithSeedAssociations(saved.getId());

        assertThat(saved).usingRecursiveComparison()
                .isEqualTo(expectedAfterSave);

        sessionFactory.getCurrentSession().clear();

        assertThat(trainingRepository.findById(saved.getId()))
                .get()
                .usingRecursiveComparison()
                .ignoringFields("trainee.trainers", "trainee.trainings", "trainer.trainees", "trainer.trainings")
                .isEqualTo(expectedFromDb);
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

    @Test
    void shouldReturnAllTrainings() {
        TrainingEntity saved = trainingRepository.save(TestDataFactory.createDefaultTraining(4L, 1L));

        assertThat(trainingRepository.findAll())
                .extracting(TrainingEntity::getId)
                .contains(saved.getId());
    }

    @Test
    void shouldDetectExistingTrainingsByTraineeId() {
        trainingRepository.save(TestDataFactory.createDefaultTraining(4L, 1L));

        assertThat(trainingRepository.existsByTraineeId(4L)).isTrue();
        assertThat(trainingRepository.existsByTraineeId(999L)).isFalse();
    }

    @Test
    void shouldFindTraineeTrainingsWithoutOptionalFilters() {
        TrainingEntity saved = trainingRepository.save(TestDataFactory.createDefaultTraining(4L, 1L));

        List<TrainingEntity> actual = trainingRepository.findByTraineeUsernameAndCriteria(
                "Alice.Walker", null, null, null, null);

        assertThat(actual)
                .extracting(TrainingEntity::getId)
                .contains(saved.getId());
    }

    @Test
    void shouldFindTraineeTrainingsWithAllFilters() {
        TrainingEntity saved = trainingRepository.save(TestDataFactory.createDefaultTraining(4L, 1L));

        List<TrainingEntity> actual = trainingRepository.findByTraineeUsernameAndCriteria(
                "Alice.Walker",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31),
                "John.Smith",
                TrainingType.YOGA);

        assertThat(actual)
                .extracting(TrainingEntity::getId)
                .contains(saved.getId());
    }

    @Test
    void shouldReturnEmptyTraineeTrainingsWhenOutOfDateRange() {
        trainingRepository.save(TestDataFactory.createDefaultTraining(4L, 1L));

        List<TrainingEntity> actual = trainingRepository.findByTraineeUsernameAndCriteria(
                "Alice.Walker",
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 12, 31),
                null,
                null);

        assertThat(actual).isEmpty();
    }

    @Test
    void shouldFindTrainerTrainingsWithoutOptionalFilters() {
        TrainingEntity saved = trainingRepository.save(TestDataFactory.createDefaultTraining(4L, 1L));

        List<TrainingEntity> actual = trainingRepository.findByTrainerUsernameAndCriteria(
                "John.Smith", null, null, null);

        assertThat(actual)
                .extracting(TrainingEntity::getId)
                .contains(saved.getId());
    }

    @Test
    void shouldFindTrainerTrainingsWithAllFilters() {
        TrainingEntity saved = trainingRepository.save(TestDataFactory.createDefaultTraining(4L, 1L));

        List<TrainingEntity> actual = trainingRepository.findByTrainerUsernameAndCriteria(
                "John.Smith",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31),
                "Alice.Walker");

        assertThat(actual)
                .extracting(TrainingEntity::getId)
                .contains(saved.getId());
    }

    @Test
    void shouldReturnEmptyTrainerTrainingsWhenTraineeFilterDoesNotMatch() {
        trainingRepository.save(TestDataFactory.createDefaultTraining(4L, 1L));

        List<TrainingEntity> actual = trainingRepository.findByTrainerUsernameAndCriteria(
                "John.Smith",
                null,
                null,
                "Nonexistent.User");

        assertThat(actual).isEmpty();
    }

    @Test
    void shouldFilterTraineeTrainingsByTrainerUsernameOnly() {
        TrainingEntity saved = trainingRepository.save(TestDataFactory.createDefaultTraining(4L, 1L));

        List<TrainingEntity> actual = trainingRepository.findByTraineeUsernameAndCriteria(
                "Alice.Walker", null, null, "John.Smith", null);

        assertThat(actual).extracting(TrainingEntity::getId).contains(saved.getId());
    }

    @Test
    void shouldFilterTraineeTrainingsByTrainingTypeOnly() {
        TrainingEntity saved = trainingRepository.save(TestDataFactory.createDefaultTraining(4L, 1L));

        List<TrainingEntity> actual = trainingRepository.findByTraineeUsernameAndCriteria(
                "Alice.Walker", null, null, null, TrainingType.YOGA);

        assertThat(actual).extracting(TrainingEntity::getId).contains(saved.getId());
    }

    @Test
    void shouldFilterTrainerTrainingsByTraineeUsernameOnly() {
        TrainingEntity saved = trainingRepository.save(TestDataFactory.createDefaultTraining(4L, 1L));

        List<TrainingEntity> actual = trainingRepository.findByTrainerUsernameAndCriteria(
                "John.Smith", null, null, "Alice.Walker");

        assertThat(actual).extracting(TrainingEntity::getId).contains(saved.getId());
    }
}
