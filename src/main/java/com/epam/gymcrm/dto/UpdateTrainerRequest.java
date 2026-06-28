package com.epam.gymcrm.dto;

import com.epam.gymcrm.entity.TrainingType;

public record UpdateTrainerRequest(
        Long userId,
        UserInfo user,
        TrainingType specialization,
        boolean active
) {}
