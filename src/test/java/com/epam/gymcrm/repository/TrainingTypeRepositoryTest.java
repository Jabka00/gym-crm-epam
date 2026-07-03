package com.epam.gymcrm.repository;

import com.epam.gymcrm.entity.TrainingTypeEntity;
import com.epam.gymcrm.support.MySqlIntegrationTest;
import com.epam.gymcrm.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@MySqlIntegrationTest
class TrainingTypeRepositoryTest {

    @Autowired
    private TrainingTypeRepository trainingTypeRepository;

    @Test
    void shouldFindById() {
        TrainingTypeEntity expected = TestDataFactory.yogaTypeEntity();

        assertThat(trainingTypeRepository.findById(1L))
                .get()
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    void shouldFindByTypeName() {
        TrainingTypeEntity expected = TestDataFactory.yogaTypeEntity();
        expected.setTypeName("CROSSFIT");
        expected.setId(2L);

        assertThat(trainingTypeRepository.findByTypeName("CROSSFIT"))
                .get()
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    void shouldFindAll() {
        List<TrainingTypeEntity> expected = List.of(
                TestDataFactory.yogaTypeEntity(),
                TestDataFactory.crossfitTypeEntity(),
                TestDataFactory.boxingTypeEntity(),
                TestDataFactory.pilatesTypeEntity()
        );

        assertThat(trainingTypeRepository.findAll())
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    void shouldReturnEmptyWhenNotFound() {
        assertThat(trainingTypeRepository.findById(99L)).isEmpty();
        assertThat(trainingTypeRepository.findByTypeName("UNKNOWN")).isEmpty();
    }
}
