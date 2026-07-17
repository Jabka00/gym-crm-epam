package com.epam.gymcrm.repository;

import com.epam.gymcrm.entity.TrainingEntity;
import com.epam.gymcrm.model.TrainingType;
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
    void shouldSaveTraining() {
        TrainingEntity input = TestDataFactory.createDefaultTraining(4L, 1L);

        TrainingEntity saved = trainingRepository.save(input);

        assertThat(saved.getId()).isNotNull();
        assertThat(trainingRepository.findByTraineeUsernameAndCriteria(
                "Alice.Walker", null, null, null, null))
                .extracting(TrainingEntity::getId)
                .contains(saved.getId());
    }

    @Test
    void shouldUpdateExistingTrainingOnSave() {
        TrainingEntity saved = trainingRepository.save(TestDataFactory.createDefaultTraining(4L, 1L));
        saved.setTrainingName("Evening Yoga");
        saved.setDurationMinutes(90);

        TrainingEntity updated = trainingRepository.save(saved);

        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();

        assertThat(updated.getTrainingName()).isEqualTo("Evening Yoga");
        assertThat(trainingRepository.findByTraineeUsernameAndCriteria(
                "Alice.Walker", null, null, null, null))
                .filteredOn(training -> training.getId().equals(saved.getId()))
                .singleElement()
                .satisfies(training -> {
                    assertThat(training.getTrainingName()).isEqualTo("Evening Yoga");
                    assertThat(training.getDurationMinutes()).isEqualTo(90);
                });
    }

    @Test
    void shouldLoadAssociationsWhenFetchingTraineeTrainings() {
        TrainingEntity saved = trainingRepository.save(TestDataFactory.createDefaultTraining(4L, 1L));

        TrainingEntity actual = trainingRepository.findByTraineeUsernameAndCriteria(
                        "Alice.Walker", null, null, null, null)
                .stream()
                .filter(training -> training.getId().equals(saved.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(actual.getTrainee().getId()).isEqualTo(4L);
        assertThat(actual.getTrainer().getId()).isEqualTo(1L);
        assertThat(actual.getTrainingType().getTypeName()).isEqualTo(TrainingType.YOGA);
        assertThat(actual.getTrainingName()).isEqualTo("Morning Yoga");
        assertThat(actual.getDurationMinutes()).isEqualTo(60);
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
    void shouldIgnoreBlankTrainerUsernameFilterForTraineeTrainings() {
        TrainingEntity saved = trainingRepository.save(TestDataFactory.createDefaultTraining(4L, 1L));

        List<TrainingEntity> actual = trainingRepository.findByTraineeUsernameAndCriteria(
                "Alice.Walker", null, null, "   ", null);

        assertThat(actual)
                .extracting(TrainingEntity::getId)
                .contains(saved.getId());
    }

    @Test
    void shouldFilterTraineeTrainingsByFromDateOnly() {
        TrainingEntity saved = trainingRepository.save(TestDataFactory.createDefaultTraining(4L, 1L));

        List<TrainingEntity> actual = trainingRepository.findByTraineeUsernameAndCriteria(
                "Alice.Walker", LocalDate.of(2024, 3, 1), null, null, null);

        assertThat(actual).extracting(TrainingEntity::getId).contains(saved.getId());
    }

    @Test
    void shouldFilterTraineeTrainingsByToDateOnly() {
        TrainingEntity saved = trainingRepository.save(TestDataFactory.createDefaultTraining(4L, 1L));

        List<TrainingEntity> actual = trainingRepository.findByTraineeUsernameAndCriteria(
                "Alice.Walker", null, LocalDate.of(2024, 3, 1), null, null);

        assertThat(actual).extracting(TrainingEntity::getId).contains(saved.getId());
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
    void shouldReturnEmptyTraineeTrainingsWhenTrainerFilterDoesNotMatch() {
        trainingRepository.save(TestDataFactory.createDefaultTraining(4L, 1L));

        List<TrainingEntity> actual = trainingRepository.findByTraineeUsernameAndCriteria(
                "Alice.Walker", null, null, "Anna.Jones", null);

        assertThat(actual).isEmpty();
    }

    @Test
    void shouldReturnEmptyTraineeTrainingsWhenTypeFilterDoesNotMatch() {
        trainingRepository.save(TestDataFactory.createDefaultTraining(4L, 1L));

        List<TrainingEntity> actual = trainingRepository.findByTraineeUsernameAndCriteria(
                "Alice.Walker", null, null, "John.Smith", TrainingType.PILATES);

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
    void shouldIgnoreBlankTraineeUsernameFilterForTrainerTrainings() {
        TrainingEntity saved = trainingRepository.save(TestDataFactory.createDefaultTraining(4L, 1L));

        List<TrainingEntity> actual = trainingRepository.findByTrainerUsernameAndCriteria(
                "John.Smith", null, null, " ");

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

    @Test
    void shouldFindSeedTrainingsForTrainee() {
        List<TrainingEntity> actual = trainingRepository.findByTraineeUsernameAndCriteria(
                "Alice.Walker", null, null, null, null);

        assertThat(actual)
                .extracting(TrainingEntity::getTrainingName)
                .contains("Morning Yoga", "Boxing Basics");
    }
}
