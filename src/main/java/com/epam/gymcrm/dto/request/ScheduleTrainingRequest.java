package com.epam.gymcrm.dto.request;

import com.epam.gymcrm.model.TrainingType;

import java.time.Duration;
import java.time.LocalDate;

public record ScheduleTrainingRequest(
        Long traineeId,
        Long trainerId,
        String name,
        TrainingType type,
        LocalDate date,
        Duration duration
) {}
