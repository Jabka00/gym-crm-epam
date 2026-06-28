package com.epam.gymcrm.dto;

import java.time.LocalDate;

public record CreateTraineeRequest(
        UserInfo user,
        LocalDate dateOfBirth,
        String address
) {}
