package com.epam.gymcrm.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
public class Trainer extends User {
    private TrainingType specialization;

    public boolean matchesSpecialization(String trainingTypeName) {
        return specialization != null
                && Objects.equals(specialization.trainingTypeName(), trainingTypeName);
    }
}
