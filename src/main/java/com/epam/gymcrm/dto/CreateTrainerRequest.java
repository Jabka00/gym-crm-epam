package com.epam.gymcrm.dto;

import com.epam.gymcrm.entity.TrainingType;

public record CreateTrainerRequest(
        UserInfo user,
        TrainingType specialization
) {}
