package com.epam.gymcrm.dto.request;

import java.time.LocalDate;

public record CreateTraineeRequest(
        UserInfo user,
        LocalDate dateOfBirth,
        String address
) {}
