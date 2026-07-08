package com.epam.gymcrm.dto.response;

import java.time.LocalDate;

public record Trainee(
        Long userId,
        String fullName,
        String username,
        LocalDate dateOfBirth,
        String address
) {
}
