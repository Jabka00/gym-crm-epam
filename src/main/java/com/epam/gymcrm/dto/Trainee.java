package com.epam.gymcrm.dto;

import java.time.LocalDate;

public record Trainee(
        Long userId,
        String fullName,
        String username,
        LocalDate dateOfBirth,
        String address
) {
}
