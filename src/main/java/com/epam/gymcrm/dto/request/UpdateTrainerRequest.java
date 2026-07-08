package com.epam.gymcrm.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateTrainerRequest(
        @NotNull(message = "Trainer id is required")
        Long id,

        @NotNull(message = "User info is required")
        @Valid
        UserInfo user,

        boolean active,

        @NotBlank(message = "Specialization is required")
        @Size(max = 50, message = "Specialization must not exceed 50 characters")
        String specialization
) {
}
