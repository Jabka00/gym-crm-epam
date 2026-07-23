package com.epam.gymcrm.dto.request;

import com.epam.gymcrm.validation.ValidPassword;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank(message = "Username cannot be null or empty")
        @Size(max = 100, message = "Username must not exceed 100 characters")
        String username,

        @NotBlank(message = "Old password cannot be null or empty")
        String oldPassword,

        @NotBlank(message = "New password cannot be null or empty")
        @ValidPassword
        String newPassword
) {
}
