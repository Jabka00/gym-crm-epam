package com.epam.gymcrm.repository;

import com.epam.gymcrm.model.TrainingType;
import com.epam.gymcrm.support.MySqlIntegrationTest;
import com.epam.gymcrm.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@MySqlIntegrationTest
class TrainingTypeRepositoryTest {

    @Autowired
    private TrainingTypeRepository trainingTypeRepository;

    @ParameterizedTest
    @EnumSource(TrainingType.class)
    void shouldFindAllSeedTrainingTypesByName(TrainingType type) {
        assertThat(trainingTypeRepository.findByTypeName(type))
                .isPresent()
                .get()
                .extracting(entity -> entity.getTypeName())
                .isEqualTo(type);
    }

    @Test
    void shouldFindCrossfitWithExpectedId() {
        assertThat(trainingTypeRepository.findByTypeName(TrainingType.CROSSFIT))
                .get()
                .usingRecursiveComparison()
                .isEqualTo(TestDataFactory.crossfitTypeEntity());
    }

    @Test
    void shouldReturnEmptyWhenTypeNameIsNull() {
        assertThat(trainingTypeRepository.findByTypeName(null)).isEmpty();
    }
}
