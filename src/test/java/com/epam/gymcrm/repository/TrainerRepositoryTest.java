package com.epam.gymcrm.repository;

import com.epam.gymcrm.entity.TraineeEntity;
import com.epam.gymcrm.entity.TrainerEntity;
import com.epam.gymcrm.model.TrainingType;
import com.epam.gymcrm.support.MySqlIntegrationTest;
import com.epam.gymcrm.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@MySqlIntegrationTest
class TrainerRepositoryTest {

    @Autowired
    private TrainerRepository trainerRepository;

    @Autowired
    private TraineeRepository traineeRepository;

    @Test
    void shouldSaveAndFindTrainerById() {
        TrainerEntity input = TestDataFactory.trainer("Trainer.One");

        TrainerEntity saved = trainerRepository.save(input);
        TrainerEntity expected = TestDataFactory.trainer("Trainer.One");
        expected.setId(saved.getId());
        expected.getUser().setId(saved.getId());

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
        saved.getUser().setFirstName("Jonathan");

        TrainerEntity updated = trainerRepository.save(saved);
        TrainerEntity expected = TestDataFactory.trainer("Trainer.Two");
        expected.setId(saved.getId());
        expected.getUser().setId(saved.getId());
        expected.getUser().setFirstName("Jonathan");

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

    @Test
    void shouldFindTrainerByUsername() {
        TrainerEntity saved = trainerRepository.save(TestDataFactory.trainer("Find.ByUsername"));
        TrainerEntity expected = TestDataFactory.trainer("Find.ByUsername");
        expected.setId(saved.getId());
        expected.getUser().setId(saved.getId());

        assertThat(trainerRepository.findByUsername("Find.ByUsername"))
                .get()
                .usingRecursiveComparison()
                .ignoringFields("trainees", "trainings")
                .isEqualTo(expected);
    }

    @Test
    void shouldFindSeedTrainerByUsernameWithSpecialization() {
        assertThat(trainerRepository.findByUsername("John.Smith"))
                .get()
                .satisfies(trainer -> {
                    assertThat(trainer.getUser().getUsername()).isEqualTo("John.Smith");
                    assertThat(trainer.getSpecialization().getTypeName()).isEqualTo(TrainingType.YOGA);
                });
    }

    @Test
    void shouldReturnEmptyWhenFindByUsernameMissing() {
        assertThat(trainerRepository.findByUsername("No.Such.Trainer")).isEmpty();
    }

    @Test
    void shouldReturnEmptyListWhenFindByUsernamesNull() {
        assertThat(trainerRepository.findByUsernames(null)).isEmpty();
    }

    @Test
    void shouldReturnEmptyListWhenFindByUsernamesEmpty() {
        assertThat(trainerRepository.findByUsernames(Set.of())).isEmpty();
    }

    @Test
    void shouldFindTrainersByUsernames() {
        TrainerEntity first = trainerRepository.save(TestDataFactory.trainer("Batch.One"));
        TrainerEntity second = trainerRepository.save(TestDataFactory.trainer("Batch.Two"));

        List<TrainerEntity> actual = trainerRepository.findByUsernames(Set.of("Batch.One", "Batch.Two"));

        assertThat(actual)
                .extracting(TrainerEntity::getId)
                .containsExactlyInAnyOrder(first.getId(), second.getId());
    }

    @Test
    void shouldIgnoreMissingUsernamesWhenFindingByUsernames() {
        List<TrainerEntity> actual = trainerRepository.findByUsernames(
                Set.of("John.Smith", "Missing.Trainer"));

        assertThat(actual)
                .extracting(trainer -> trainer.getUser().getUsername())
                .containsExactly("John.Smith");
    }

    @Test
    void shouldFindTrainersNotAssignedToTrainee() {
        TrainerEntity trainer = trainerRepository.save(TestDataFactory.trainer("Unassigned.Trainer"));

        List<TrainerEntity> actual = trainerRepository.findNotAssignedToTrainee("Nobody.Assigned");

        assertThat(actual)
                .extracting(TrainerEntity::getId)
                .contains(trainer.getId());
    }

    @Test
    void shouldExcludeAssignedTrainerFromNotAssignedList() {
        List<TrainerEntity> actual = trainerRepository.findNotAssignedToTrainee("Alice.Walker");

        assertThat(actual)
                .extracting(trainer -> trainer.getUser().getUsername())
                .doesNotContain("John.Smith")
                .contains("Anna.Jones");
    }

    @Test
    void shouldExcludeInactiveTrainerFromNotAssignedList() {
        List<TrainerEntity> actual = trainerRepository.findNotAssignedToTrainee("Alice.Walker");

        assertThat(actual)
                .extracting(trainer -> trainer.getUser().getUsername())
                .doesNotContain("Mike.Brown");
    }

    @Test
    void shouldExcludeNewlyAssignedTrainerFromNotAssignedList() {
        TraineeEntity trainee = traineeRepository.save(TestDataFactory.trainee("Assign.Check"));
        TrainerEntity trainer = trainerRepository.save(TestDataFactory.trainer("Soon.Assigned"));
        trainee.getTrainers().add(trainer);
        traineeRepository.save(trainee);

        List<TrainerEntity> actual = trainerRepository.findNotAssignedToTrainee("Assign.Check");

        assertThat(actual)
                .extracting(TrainerEntity::getId)
                .doesNotContain(trainer.getId());
    }
}
