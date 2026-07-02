package com.epam.gymcrm.dto.request;

import java.time.Duration;
import java.time.LocalDate;

public record ScheduleTrainingRequest(
        Long traineeId,
        Long trainerId,
        String name,
        String type,
        LocalDate date,
        Duration duration
) {}
