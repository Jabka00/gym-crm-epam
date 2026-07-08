package com.epam.gymcrm.dto.response;

import java.time.LocalDate;

public record Training(
        Long id,
        String name,
        TrainingType type,
        LocalDate date,
        Integer duration,
        Long traineeId,
        Long trainerId
) {
}
