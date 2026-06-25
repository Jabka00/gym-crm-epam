package com.epam.gymcrm.dto;

import com.epam.gymcrm.model.TrainingType;

import java.time.Duration;
import java.time.LocalDate;

public record ScheduleTrainingRequest(
        Long traineeId,
        Long trainerId,
        String trainingName,
        TrainingType trainingType,
        LocalDate trainingDate,
        Duration trainingDuration
) {}
