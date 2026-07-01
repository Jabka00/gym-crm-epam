package com.epam.gymcrm.dto.request;

import com.epam.gymcrm.model.TrainingType;

public record CreateTrainerRequest(
        UserInfo user,
        TrainingType specialization
) {}
