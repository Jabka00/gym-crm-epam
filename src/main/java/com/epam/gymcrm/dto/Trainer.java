package com.epam.gymcrm.dto;

import com.epam.gymcrm.model.TrainingType;

public record Trainer(
        Long userId,
        String fullName,
        String username,
        TrainingType specialization
) {
}
