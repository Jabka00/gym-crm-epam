package com.epam.gymcrm.dto.response;

public record Trainer(
        Long userId,
        String fullName,
        String username,
        TrainingType specialization
) {
}
