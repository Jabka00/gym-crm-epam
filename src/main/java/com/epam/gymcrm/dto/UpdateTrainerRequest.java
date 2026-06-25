package com.epam.gymcrm.dto;

import com.epam.gymcrm.model.TrainingType;

public record UpdateTrainerRequest(
        Long userId,
        String firstName,
        String lastName,
        TrainingType specialization,
        boolean active
) {}
