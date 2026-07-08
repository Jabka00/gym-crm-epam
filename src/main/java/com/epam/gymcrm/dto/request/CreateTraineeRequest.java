package com.epam.gymcrm.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateTraineeRequest(
        @NotNull(message = "User info is required")
        @Valid
        UserInfo user,

        @Past(message = "Date of birth must be in the past")
        LocalDate dateOfBirth,

        @Size(max = 255, message = "Address must not exceed 255 characters")
        String address
) {
}
