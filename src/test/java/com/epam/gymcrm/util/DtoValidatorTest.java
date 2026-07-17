package com.epam.gymcrm.util;

import com.epam.gymcrm.model.TrainingType;
import com.epam.gymcrm.exception.ValidationException;

import com.epam.gymcrm.dto.request.CreateTraineeRequest;
import com.epam.gymcrm.dto.request.CreateTrainerRequest;
import com.epam.gymcrm.dto.request.ScheduleTrainingRequest;
import com.epam.gymcrm.dto.request.UpdateTraineeRequest;
import com.epam.gymcrm.dto.request.UpdateTrainerRequest;
import com.epam.gymcrm.dto.request.UserInfo;
import com.epam.gymcrm.dto.Credentials;
import com.epam.gymcrm.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatCode;
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
                .isInstanceOf(ValidationException.class)
                .hasMessage("DTO cannot be null");
    }

    @Test
    void shouldRejectTraineeWithoutFirstName() {
        CreateTraineeRequest dto = TestDataFactory.createTraineeRequest("", "Doe");

        assertThatThrownBy(() -> dtoValidator.validate(dto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("First name is required");
    }

    @Test
    void shouldRejectTrainerWithoutSpecialization() {
        CreateTrainerRequest dto = new CreateTrainerRequest(new UserInfo("John", "Smith"), null);

        assertThatThrownBy(() -> dtoValidator.validate(dto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Specialization is required");
    }

    @Test
    void shouldRejectTrainingWithoutName() {
        assertThatThrownBy(() -> dtoValidator.validate(
                        TestDataFactory.scheduleTrainingRequestWithoutName(1L, 2L)))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Name is required");
    }

    @Test
    void shouldRejectUpdateWithoutId() {
        UpdateTraineeRequest dto = TestDataFactory.updateTraineeRequestWithoutId();

        assertThatThrownBy(() -> dtoValidator.validate(dto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Trainee id is required");
    }

    @Test
    void shouldAcceptValidCreateTraineeRequest() {
        assertThatCode(() -> dtoValidator.validate(TestDataFactory.createTraineeRequest()))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldRejectFutureDateOfBirth() {
        CreateTraineeRequest dto = new CreateTraineeRequest(
                new UserInfo("Jane", "Doe"),
                LocalDate.now().plusDays(1),
                "Kyiv");

        assertThatThrownBy(() -> dtoValidator.validate(dto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Date of birth must be in the past");
    }

    @Test
    void shouldRejectBlankCredentialsUsername() {
        assertThatThrownBy(() -> dtoValidator.validate(new Credentials("", "secret1234")))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Username cannot be null or empty");
    }

    @Test
    void shouldRejectBlankCredentialsPassword() {
        assertThatThrownBy(() -> dtoValidator.validate(new Credentials("Alice.Walker", "")))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Password cannot be null or empty");
    }

    @Test
    void shouldRejectTrainingWithTooShortDuration() {
        ScheduleTrainingRequest dto = new ScheduleTrainingRequest(
                1L, 2L, "Morning Yoga", TrainingType.YOGA, LocalDate.of(2024, 3, 1), Duration.ofSeconds(30));

        assertThatThrownBy(() -> dtoValidator.validate(dto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Duration must be at least 15 minutes");
    }

    @Test
    void shouldRejectTrainerUpdateWithoutId() {
        UpdateTrainerRequest dto = new UpdateTrainerRequest(
                null, new UserInfo("John", "Smith"), TrainingType.YOGA);

        assertThatThrownBy(() -> dtoValidator.validate(dto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Trainer id is required");
    }
}
