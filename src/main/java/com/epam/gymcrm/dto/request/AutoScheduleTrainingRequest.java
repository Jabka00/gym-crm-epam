package com.epam.gymcrm.dto.request;

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

        @NotBlank(message = "Type is required")
        @Size(max = 50, message = "Type must not exceed 50 characters")
        String type,

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
