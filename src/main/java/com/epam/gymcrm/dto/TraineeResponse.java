package com.epam.gymcrm.dto;

import java.time.LocalDate;

public record TraineeResponse(
        Long userId,
        String firstName,
        String lastName,
        String username,
        boolean active,
        LocalDate dateOfBirth,
        String address
) {}
