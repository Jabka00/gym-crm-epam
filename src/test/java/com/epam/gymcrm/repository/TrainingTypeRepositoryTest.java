package com.epam.gymcrm.repository;

import com.epam.gymcrm.dto.response.TrainingTypeResponse;
import com.epam.gymcrm.model.TrainingType;
import com.epam.gymcrm.entity.TrainingTypeEntity;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.service.TrainingTypeService;
import com.epam.gymcrm.support.MySqlIntegrationTest;
import com.epam.gymcrm.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@MySqlIntegrationTest
class TrainingTypeRepositoryTest {

    @Autowired
    private TrainingTypeRepository trainingTypeRepository;

    @Autowired
    private TrainingTypeService trainingTypeService;

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
        expected.setTypeName(TrainingType.CROSSFIT);
        expected.setId(2L);

        assertThat(trainingTypeRepository.findByTypeName(TrainingType.CROSSFIT))
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
        assertThat(trainingTypeRepository.findByTypeName(null)).isEmpty();
    }

    @Test
    void shouldReturnTrainingTypeByNameThroughService() {
        TrainingTypeResponse actual = trainingTypeService.getTrainingTypeByName(TrainingType.BOXING);

        assertThat(actual).isEqualTo(TestDataFactory.trainingTypeResponse(3L, TrainingType.BOXING));
    }

    @Test
    void shouldThrowWhenTrainingTypeMissingInService() {
        assertThatThrownBy(() -> trainingTypeService.getTrainingTypeByName(null))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Training type not found: null");
    }
}
