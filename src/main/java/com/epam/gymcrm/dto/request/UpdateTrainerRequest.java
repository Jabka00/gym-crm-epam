package com.epam.gymcrm.dto.request;

import com.epam.gymcrm.model.TrainingType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record UpdateTrainerRequest(
        @NotNull(message = "Trainer id is required")
        Long id,

        @NotNull(message = "User info is required")
        @Valid
        UserInfo user,

        boolean active,

        @NotNull(message = "Specialization is required")
        TrainingType specialization
) {
}
