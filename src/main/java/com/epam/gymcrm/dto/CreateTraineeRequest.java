package com.epam.gymcrm.dto;

import java.time.LocalDate;

public record CreateTraineeRequest(
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String address
) {}
