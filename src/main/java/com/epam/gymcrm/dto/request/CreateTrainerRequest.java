package com.epam.gymcrm.dto.request;

import com.epam.gymcrm.model.TrainingType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record CreateTrainerRequest(
        @NotNull(message = "User info is required")
        @Valid
        UserInfo user,

        @NotNull(message = "Specialization is required")
        TrainingType specialization
) {
}
