package com.epam.gymcrm.repository;

import com.epam.gymcrm.entity.TraineeEntity;
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
    private TrainingRepository trainingRepository;

    @Autowired
    private SessionFactory sessionFactory;

    @Test
    void shouldSaveAndFindTraineeById() {
        TraineeEntity input = TestDataFactory.trainee("First.User");

        TraineeEntity saved = traineeRepository.save(input);
        TraineeEntity expected = TestDataFactory.trainee("First.User");
        expected.setId(saved.getId());

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
    void shouldDeleteTrainee() {
        TraineeEntity saved = traineeRepository.save(TestDataFactory.trainee("Third.User"));

        traineeRepository.delete(saved.getId());

        assertThat(traineeRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    void shouldCascadeDeleteTrainingsWhenDeletingTraineeByUsername() {
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
        assertThat(trainingRepository.findById(trainingId)).isEmpty();
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

        assertThat(traineeRepository.findByUsername("Find.User"))
                .get()
                .usingRecursiveComparison()
                .ignoringFields("trainers", "trainings")
                .isEqualTo(expected);
    }

    @Test
    void shouldReturnEmptyWhenFindByUsernameMissing() {
        assertThat(traineeRepository.findByUsername("No.Such.User")).isEmpty();
    }

    @Test
    void shouldReturnAllTrainees() {
        TraineeEntity saved = traineeRepository.save(TestDataFactory.trainee("All.User"));

        assertThat(traineeRepository.findAll())
                .extracting(TraineeEntity::getId)
                .contains(saved.getId());
    }

    @Test
    void shouldDetectExistingTraineeByUsername() {
        traineeRepository.save(TestDataFactory.trainee("Exists.User"));

        assertThat(traineeRepository.existsByUsername("Exists.User")).isTrue();
        assertThat(traineeRepository.existsByUsername("Missing.User")).isFalse();
    }
}
