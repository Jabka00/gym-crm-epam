package com.epam.gymcrm.dto.response;

import com.epam.gymcrm.model.TrainingType;

public record TrainingTypeResponse(
        Long id,
        TrainingType typeName
) {
}
