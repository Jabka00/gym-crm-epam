package com.epam.gymcrm.repository;

import com.epam.gymcrm.config.AppConfig;
import com.epam.gymcrm.model.TrainingType;
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
@TestPropertySource(properties = "db.url=jdbc:h2:mem:training_type_repository_test;DB_CLOSE_DELAY=-1")
@Transactional
class TrainingTypeRepositoryTest {

    @Autowired
    private TrainingTypeRepository trainingTypeRepository;

    @Test
    void shouldFindById() {
        assertThat(trainingTypeRepository.findById(1L))
                .isPresent()
                .get()
                .extracting(type -> type.toEnum())
                .isEqualTo(TrainingType.YOGA);
    }

    @Test
    void shouldFindByTypeName() {
        assertThat(trainingTypeRepository.findByTypeName("CROSSFIT"))
                .isPresent()
                .get()
                .extracting(type -> type.getId())
                .isEqualTo(2L);
    }

    @Test
    void shouldFindAll() {
        assertThat(trainingTypeRepository.findAll()).hasSize(4);
    }

    @Test
    void shouldReturnEmptyWhenNotFound() {
        assertThat(trainingTypeRepository.findById(99L)).isEmpty();
        assertThat(trainingTypeRepository.findByTypeName("UNKNOWN")).isEmpty();
    }
}
