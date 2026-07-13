package com.epam.gymcrm.dto.request;

import com.epam.gymcrm.model.TrainingType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Duration;
import java.time.LocalDate;

public record AutoScheduleTrainingRequest(
        @NotNull(message = "Trainee id is required")
        Long traineeId,

        @NotBlank(message = "Name is required")
        @Size(max = 200, message = "Name must not exceed 200 characters")
        String name,

        @NotNull(message = "Type is required")
        TrainingType type,

        @NotNull(message = "Date is required")
        LocalDate date,

        @NotNull(message = "Duration is required")
        Duration duration
) {
    @AssertTrue(message = "Duration must be at least 1 minute")
    private boolean isDurationValid() {
        return duration == null || duration.compareTo(Duration.ofMinutes(1)) >= 0;
    }
}
