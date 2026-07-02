package com.epam.gymcrm.repository;

import com.epam.gymcrm.config.AppConfig;
import com.epam.gymcrm.entity.TraineeEntity;
import com.epam.gymcrm.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = AppConfig.class)
@TestPropertySource(properties = {
        "db.url=jdbc:h2:mem:trainee_repository_test;DB_CLOSE_DELAY=-1",
        "db.username=sa",
        "db.password=",
        "db.driver=org.h2.Driver",
        "hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "db.init.enabled=true"
})
@Transactional
class TraineeRepositoryTest {

    @Autowired
    private TraineeRepository traineeRepository;

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
        assertThat(traineeRepository.existsById(saved.getId())).isTrue();
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
        assertThat(traineeRepository.existsById(saved.getId())).isFalse();
    }

    @Test
    void shouldReturnEmptyWhenTraineeNotFound() {
        assertThat(traineeRepository.findById(404L)).isEmpty();
    }
}
