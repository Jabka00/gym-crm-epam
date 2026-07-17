package com.epam.gymcrm.repository;

import com.epam.gymcrm.entity.TraineeEntity;
import com.epam.gymcrm.entity.TrainerEntity;
import com.epam.gymcrm.entity.TrainingEntity;
import com.epam.gymcrm.support.MySqlIntegrationTest;
import com.epam.gymcrm.support.TestDataFactory;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@MySqlIntegrationTest
class TraineeRepositoryTest {

    @Autowired
    private TraineeRepository traineeRepository;

    @Autowired
    private TrainerRepository trainerRepository;

    @Autowired
    private TrainingRepository trainingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SessionFactory sessionFactory;

    @Test
    void shouldSaveAndFindTraineeById() {
        TraineeEntity input = TestDataFactory.trainee("First.User");

        TraineeEntity saved = traineeRepository.save(input);
        TraineeEntity expected = TestDataFactory.trainee("First.User");
        expected.setId(saved.getId());
        expected.getUser().setId(saved.getId());

        assertThat(saved).usingRecursiveComparison()
                .ignoringFields("trainers", "trainings")
                .isEqualTo(expected);
        assertThat(traineeRepository.findById(saved.getId()))
                .get()
                .usingRecursiveComparison()
                .ignoringFields("trainers", "trainings")
                .isEqualTo(expected);
    }

    @Test
    void shouldOverwriteExistingTraineeOnSave() {
        TraineeEntity saved = traineeRepository.save(TestDataFactory.trainee("Second.User"));
        saved.setAddress("Odesa");

        TraineeEntity updated = traineeRepository.save(saved);
        TraineeEntity expected = TestDataFactory.trainee("Second.User");
        expected.setId(saved.getId());
        expected.getUser().setId(saved.getId());
        expected.setAddress("Odesa");

        assertThat(updated).usingRecursiveComparison()
                .ignoringFields("trainers", "trainings")
                .isEqualTo(expected);
        assertThat(traineeRepository.findById(saved.getId()))
                .get()
                .usingRecursiveComparison()
                .ignoringFields("trainers", "trainings")
                .isEqualTo(expected);
    }

    @Test
    void shouldReturnEmptyWhenTraineeNotFound() {
        assertThat(traineeRepository.findById(404L)).isEmpty();
    }

    @Test
    void shouldFindTraineeByUsername() {
        TraineeEntity saved = traineeRepository.save(TestDataFactory.trainee("Find.User"));
        TraineeEntity expected = TestDataFactory.trainee("Find.User");
        expected.setId(saved.getId());
        expected.getUser().setId(saved.getId());

        assertThat(traineeRepository.findByUsername("Find.User"))
                .get()
                .usingRecursiveComparison()
                .ignoringFields("trainers", "trainings")
                .isEqualTo(expected);
    }

    @Test
    void shouldFindSeedTraineeWithAssignedTrainers() {
        assertThat(traineeRepository.findByUsername("Alice.Walker"))
                .get()
                .satisfies(trainee -> {
                    assertThat(trainee.getUser().getUsername()).isEqualTo("Alice.Walker");
                    assertThat(trainee.getTrainers())
                            .extracting(trainer -> trainer.getUser().getUsername())
                            .contains("John.Smith");
                });
    }

    @Test
    void shouldReturnEmptyWhenFindByUsernameMissing() {
        assertThat(traineeRepository.findByUsername("No.Such.User")).isEmpty();
    }

    @Test
    void shouldDeleteByUsernameSilentlyWhenTraineeMissing() {
        traineeRepository.deleteByUsername("Definitely.Missing.User");

        assertThat(traineeRepository.findByUsername("Definitely.Missing.User")).isEmpty();
        assertThat(userRepository.findByUsername("Definitely.Missing.User")).isEmpty();
    }

    @Test
    void shouldHardDeleteTraineeTrainingsAndUser() {
        TraineeEntity trainee = traineeRepository.save(TestDataFactory.trainee("Cascade.User"));
        TrainingEntity training = trainingRepository.save(
                TestDataFactory.createDefaultTraining(trainee.getId(), 1L));
        Long trainingId = training.getId();

        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();

        traineeRepository.deleteByUsername("Cascade.User");

        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();

        assertThat(traineeRepository.findByUsername("Cascade.User")).isEmpty();
        assertThat(userRepository.findByUsername("Cascade.User")).isEmpty();
        assertThat(trainingRepository.findByTraineeUsernameAndCriteria(
                "Cascade.User", null, null, null, null)).isEmpty();
        assertThat(trainingId).isNotNull();
        assertThat(trainerRepository.findById(1L)).isPresent();
    }

    @Test
    void shouldClearTrainerAssignmentsOnDeleteWithoutRemovingTrainer() {
        TraineeEntity trainee = traineeRepository.save(TestDataFactory.trainee("Linked.User"));
        TrainerEntity trainer = trainerRepository.save(TestDataFactory.trainer("Linked.Trainer"));
        trainee.getTrainers().add(trainer);
        traineeRepository.save(trainee);

        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();

        assertThat(traineeRepository.findByUsername("Linked.User"))
                .get()
                .satisfies(found -> assertThat(found.getTrainers()).isNotEmpty());

        traineeRepository.deleteByUsername("Linked.User");

        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();

        assertThat(traineeRepository.findByUsername("Linked.User")).isEmpty();
        assertThat(userRepository.findByUsername("Linked.User")).isEmpty();
        assertThat(trainerRepository.findByUsername("Linked.Trainer")).isPresent();
    }

    @Test
    void shouldPersistTrainerAssignments() {
        TraineeEntity trainee = traineeRepository.save(TestDataFactory.trainee("With.Trainer"));
        TrainerEntity trainer = trainerRepository.save(TestDataFactory.trainer("Assigned.One"));
        trainee.getTrainers().add(trainer);
        traineeRepository.save(trainee);

        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();

        assertThat(traineeRepository.findById(trainee.getId()))
                .get()
                .satisfies(found -> assertThat(found.getTrainers())
                        .extracting(assigned -> assigned.getUser().getUsername())
                        .containsExactly("Assigned.One"));
    }
}
