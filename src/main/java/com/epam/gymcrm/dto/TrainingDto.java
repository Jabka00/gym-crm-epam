package com.epam.gymcrm.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"trainee", "trainer", "trainingName"})
public class TrainingDto {

    private Long id;

    @NotNull(message = "Trainee is required")
    private TraineeDto trainee;

    private TrainerDto trainer;

    @NotBlank(message = "Training name is required")
    @Size(max = 200, message = "Training name must not exceed 200 characters")
    private String trainingName;

    @NotNull(message = "Training type is required")
    @Valid
    private TrainingTypeDto trainingType;

    @NotNull(message = "Training date is required")
    private LocalDate trainingDate;

    @NotNull(message = "Training duration is required")
    @Min(value = 1, message = "Training duration must be at least 1 minute")
    private Integer trainingDuration;
}
