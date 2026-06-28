package com.epam.gymcrm.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class TrainerEntity extends UserEntity {
    private TrainingType specialization;

    public boolean matchesSpecialization(TrainingType type) {
        return type != null && type == specialization;
    }
}
