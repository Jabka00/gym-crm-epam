package com.epam.gymcrm.dto.request;

import java.time.Duration;
import java.time.LocalDate;

public record AutoScheduleTrainingRequest(
        Long traineeId,
        String name,
        String type,
        LocalDate date,
        Duration duration
) {}
