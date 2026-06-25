package com.epam.gymcrm.dto;

import com.epam.gymcrm.model.TrainingType;

public record TrainerResponse(
        Long userId,
        String firstName,
        String lastName,
        String username,
        boolean active,
        TrainingType specialization
) {}
