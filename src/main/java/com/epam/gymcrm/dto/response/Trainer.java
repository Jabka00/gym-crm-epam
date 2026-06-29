package com.epam.gymcrm.dto.response;

import com.epam.gymcrm.model.TrainingType;

public record Trainer(
        Long userId,
        String fullName,
        String username,
        TrainingType specialization
) {}
