package com.epam.gymcrm.dto.request;

import com.epam.gymcrm.model.TrainingType;

public record UpdateTrainerRequest(
        Long userId,
        UserInfo user,
        TrainingType specialization,
        boolean active
) {}
