package com.epam.gymcrm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingTypeDto {

    private Long id;

    @NotBlank(message = "Type name is required")
    @Size(max = 50, message = "Type name must not exceed 50 characters")
    private String typeName;
}
