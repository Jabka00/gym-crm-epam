package com.epam.gymcrm.mapper;

import com.epam.gymcrm.dto.TrainerResponse;
import com.epam.gymcrm.entity.TrainerEntity;
import org.springframework.stereotype.Component;

@Component
public class TrainerMapper {

    public TrainerResponse toResponse(TrainerEntity trainer) {
        return new TrainerResponse(
                trainer.getUserId(),
                trainer.getUsername(),
                trainer.getSpecialization()
        );
    }
}
