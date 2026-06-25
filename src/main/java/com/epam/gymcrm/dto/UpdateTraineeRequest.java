package com.epam.gymcrm.dto;

import java.time.LocalDate;

public record UpdateTraineeRequest(
        Long userId,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String address,
        boolean active
) {}
