package com.epam.gymcrm.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record AutoScheduleTrainingRequest(
        @NotNull(message = "Trainee id is required")
        Long traineeId,

        @NotBlank(message = "Training name is required")
        @Size(max = 200, message = "Training name must not exceed 200 characters")
        String trainingName,

        @NotBlank(message = "Training type is required")
        @Size(max = 50, message = "Training type must not exceed 50 characters")
        String trainingType,

        @NotNull(message = "Training date is required")
        LocalDate trainingDate,

        @NotNull(message = "Training duration is required")
        @Min(value = 1, message = "Training duration must be at least 1 minute")
        Integer trainingDuration
) {
}
