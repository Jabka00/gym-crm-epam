package com.epam.gymcrm.dto;

import com.epam.gymcrm.model.TrainingType;

import java.time.Duration;
import java.time.LocalDate;

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
