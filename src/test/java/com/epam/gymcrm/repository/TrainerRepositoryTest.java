package com.epam.gymcrm.repository;

import com.epam.gymcrm.entity.TrainerEntity;
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

    @Test
    void shouldFindTrainerByUsername() {
        TrainerEntity saved = trainerRepository.save(TestDataFactory.trainer("Find.ByUsername"));
        TrainerEntity expected = TestDataFactory.trainer("Find.ByUsername");
        expected.setId(saved.getId());

        assertThat(trainerRepository.findByUsername("Find.ByUsername"))
                .get()
                .usingRecursiveComparison()
                .ignoringFields("trainees", "trainings")
                .isEqualTo(expected);
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
    void shouldReturnAllTrainers() {
        TrainerEntity saved = trainerRepository.save(TestDataFactory.trainer("All.Trainer"));

        assertThat(trainerRepository.findAll())
                .extracting(TrainerEntity::getId)
                .contains(saved.getId());
    }

    @Test
    void shouldDetectExistingTrainerByUsername() {
        trainerRepository.save(TestDataFactory.trainer("Exists.Trainer"));

        assertThat(trainerRepository.existsByUsername("Exists.Trainer")).isTrue();
        assertThat(trainerRepository.existsByUsername("Missing.Trainer")).isFalse();
    }

    @Test
    void shouldFindActiveTrainerBySpecialization() {
        TrainerEntity saved = trainerRepository.save(TestDataFactory.trainer("Active.Yoga"));
        assertThat(saved.isActive()).isTrue();

        TrainerEntity found = trainerRepository.findActiveBySpecialization("YOGA").orElseThrow();

        assertThat(found.isActive()).isTrue();
        assertThat(found.getSpecialization().getTypeName()).isEqualTo("YOGA");
    }

    @Test
    void shouldNotFindInactiveTrainerBySpecialization() {
        TrainerEntity inactive = TestDataFactory.trainer("Inactive.Yoga");
        inactive.setActive(false);
        trainerRepository.save(inactive);

        assertThat(trainerRepository.findActiveBySpecialization("NON_EXISTENT_TYPE")).isEmpty();
    }

    @Test
    void shouldFindTrainersNotAssignedToTrainee() {
        TrainerEntity trainer = trainerRepository.save(TestDataFactory.trainer("Unassigned.Trainer"));

        List<TrainerEntity> actual = trainerRepository.findNotAssignedToTrainee("Nobody.Assigned");

        assertThat(actual)
                .extracting(TrainerEntity::getId)
                .contains(trainer.getId());
    }
}
