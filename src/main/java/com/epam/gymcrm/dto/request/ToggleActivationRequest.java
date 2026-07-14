package com.epam.gymcrm.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ToggleActivationRequest(
        @NotBlank(message = "Username cannot be null or empty")
        @Size(max = 100, message = "Username must not exceed 100 characters")
        String username
) {
}
