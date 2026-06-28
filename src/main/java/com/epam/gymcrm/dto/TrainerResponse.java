package com.epam.gymcrm.dto;

import com.epam.gymcrm.entity.TrainingType;

public record TrainerResponse(
        Long userId,
        String fullName,
        String username,
        TrainingType specialization
) {}
