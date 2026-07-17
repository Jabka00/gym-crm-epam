package com.epam.gymcrm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record Credentials(
        @NotBlank(message = "Username cannot be null or empty")
        @Size(max = 100, message = "Username must not exceed 100 characters")
        String username,

        @NotBlank(message = "Password cannot be null or empty")
        String password
) {
}
