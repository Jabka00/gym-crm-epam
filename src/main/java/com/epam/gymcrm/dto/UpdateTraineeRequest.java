package com.epam.gymcrm.dto;

import java.time.LocalDate;

public record UpdateTraineeRequest(
        Long userId,
        UserInfo user,
        LocalDate dateOfBirth,
        String address,
        boolean active
) {}
