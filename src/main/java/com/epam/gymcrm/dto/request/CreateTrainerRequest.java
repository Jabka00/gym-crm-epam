package com.epam.gymcrm.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateTrainerRequest(
        @NotNull(message = "User info is required")
        @Valid
        UserInfo user,

        @NotBlank(message = "Specialization is required")
        @Size(max = 50, message = "Specialization must not exceed 50 characters")
        String specialization
) {
}
