package com.epam.gymcrm.util;

import com.epam.gymcrm.dto.TraineeDto;
import com.epam.gymcrm.dto.TrainerDto;
import com.epam.gymcrm.dto.TrainingDto;
import com.epam.gymcrm.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DtoValidatorTest {

    private DtoValidator dtoValidator;

    @BeforeEach
    void setUp() {
        dtoValidator = new DtoValidator();
    }

    @Test
    void shouldRejectNullDto() {
        assertThatThrownBy(() -> dtoValidator.validate(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("DTO cannot be null");
    }

    @Test
    void shouldRejectTraineeWithoutFirstName() {
        TraineeDto dto = TraineeDto.builder().lastName("Doe").build();

        assertThatThrownBy(() -> dtoValidator.validate(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("First name is required");
    }

    @Test
    void shouldRejectTrainerWithoutSpecialization() {
        TrainerDto dto = TrainerDto.builder().firstName("John").lastName("Smith").build();

        assertThatThrownBy(() -> dtoValidator.validate(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Specialization is required");
    }

    @Test
    void shouldRejectTrainingWithoutName() {
        TrainingDto dto = TestDataFactory.trainingDto(1L, 2L);
        dto.setTrainingName(null);

        assertThatThrownBy(() -> dtoValidator.validate(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Training name is required");
    }
}
