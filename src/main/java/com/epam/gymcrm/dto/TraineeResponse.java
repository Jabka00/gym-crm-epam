package com.epam.gymcrm.dto;

import java.time.LocalDate;

public record TraineeResponse(
        Long userId,
        String username,
        LocalDate dateOfBirth,
        String address
) {}
