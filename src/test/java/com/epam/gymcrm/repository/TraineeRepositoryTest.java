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
        TraineeEntity trainee = TestDataFactory.createTraineeWithCredentials();
        trainee.setId(100L);

        TraineeEntity saved = traineeRepository.save(trainee);

        assertThat(saved.getId()).isEqualTo(100L);
        assertThat(traineeRepository.findById(100L)).isPresent();
        assertThat(traineeRepository.existsById(100L)).isTrue();
    }

    @Test
    void shouldOverwriteExistingTraineeOnSave() {
        TraineeEntity trainee = TestDataFactory.createTraineeWithCredentials();
        trainee.setId(101L);
        traineeRepository.save(trainee);
        trainee.setAddress("Odesa");

        TraineeEntity updated = traineeRepository.save(trainee);

        assertThat(updated.getAddress()).isEqualTo("Odesa");
        assertThat(traineeRepository.findById(101L))
                .get()
                .extracting(TraineeEntity::getAddress)
                .isEqualTo("Odesa");
    }

    @Test
    void shouldDeleteTrainee() {
        TraineeEntity trainee = TestDataFactory.createTraineeWithCredentials();
        trainee.setId(102L);
        traineeRepository.save(trainee);

        traineeRepository.delete(102L);

        assertThat(traineeRepository.findById(102L)).isEmpty();
        assertThat(traineeRepository.existsById(102L)).isFalse();
    }

    @Test
    void shouldSaveMultipleTrainees() {
        TraineeEntity first = TestDataFactory.createTraineeWithCredentials();
        first.setId(103L);
        first.setUsername("First.User");
        TraineeEntity second = TestDataFactory.createDefaultTrainee();
        second.setId(104L);
        second.setUsername("Second.User");

        traineeRepository.save(first);
        traineeRepository.save(second);

        assertThat(traineeRepository.findAll().map(TraineeEntity::getId).toList())
                .contains(103L, 104L);
    }

    @Test
    void shouldReturnEmptyWhenTraineeNotFound() {
        assertThat(traineeRepository.findById(404L)).isEmpty();
    }
}
