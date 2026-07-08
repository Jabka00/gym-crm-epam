package com.epam.gymcrm.security;

import jakarta.validation.constraints.NotBlank;

public record Credentials(
        @NotBlank(message = "Username cannot be null or empty") String username,
        @NotBlank(message = "Password cannot be null or empty") String password
) {
}