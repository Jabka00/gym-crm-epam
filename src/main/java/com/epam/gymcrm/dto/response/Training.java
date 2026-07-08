package com.epam.gymcrm.dto.response;

import java.time.LocalDate;
import java.time.Duration;

public record Training(
        Long id,
        String name,
        TrainingType type,
        LocalDate date,
        Duration duration,
        Long traineeId,
        Long trainerId
) {
}
