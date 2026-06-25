package com.epam.gymcrm.dto;

import com.epam.gymcrm.model.TrainingType;

public record CreateTrainerRequest(
        String firstName,
        String lastName,
        TrainingType specialization
) {}
