package com.epam.gymcrm.mapper;

import com.epam.gymcrm.dto.TrainingResponse;
import com.epam.gymcrm.entity.TrainingEntity;
import org.springframework.stereotype.Component;

@Component
public class TrainingMapper {

    public TrainingResponse toResponse(TrainingEntity training) {
        return new TrainingResponse(
                training.getTrainingId(),
                training.getTrainingName(),
                training.getTrainingType(),
                training.getTrainingDate(),
                training.getTrainingDuration(),
                training.getTraineeId(),
                training.getTrainerId()
        );
    }
}
