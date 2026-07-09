package com.epam.gymcrm.util;

import com.epam.gymcrm.dto.request.CreateTraineeRequest;
import com.epam.gymcrm.dto.request.CreateTrainerRequest;
import com.epam.gymcrm.dto.request.UpdateTraineeRequest;
import com.epam.gymcrm.dto.request.UserInfo;
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
        CreateTraineeRequest dto = TestDataFactory.createTraineeRequest("", "Doe");

        assertThatThrownBy(() -> dtoValidator.validate(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("First name is required");
    }

    @Test
    void shouldRejectTrainerWithoutSpecialization() {
        CreateTrainerRequest dto = new CreateTrainerRequest(new UserInfo("John", "Smith"), null);

        assertThatThrownBy(() -> dtoValidator.validate(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Specialization is required");
    }

    @Test
    void shouldRejectTrainingWithoutName() {
        assertThatThrownBy(() -> dtoValidator.validate(
                        TestDataFactory.scheduleTrainingRequestWithoutName(1L, 2L)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Name is required");
    }

    @Test
    void shouldRejectUpdateWithoutId() {
        UpdateTraineeRequest dto = TestDataFactory.updateTraineeRequestWithoutId();

        assertThatThrownBy(() -> dtoValidator.validateForUpdate(dto, UpdateTraineeRequest::id, "Trainee"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Trainee id is required");
    }
}
