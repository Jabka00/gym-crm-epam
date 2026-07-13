package com.epam.gymcrm.dto.response;

import java.time.Duration;
import java.time.LocalDate;

public record Training(
        Long id,
        String name,
        TrainingTypeResponse type,
        LocalDate date,
        Duration duration,
        Long traineeId,
        Long trainerId
) {
}
