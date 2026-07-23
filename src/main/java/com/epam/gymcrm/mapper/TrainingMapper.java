package com.epam.gymcrm.mapper;

import com.epam.gymcrm.dto.Training;
import com.epam.gymcrm.dto.request.ScheduleTrainingRequest;
import com.epam.gymcrm.entity.TraineeEntity;
import com.epam.gymcrm.entity.TrainerEntity;
import com.epam.gymcrm.entity.TrainingEntity;
import com.epam.gymcrm.entity.TrainingTypeEntity;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class TrainingMapper {

    public Training toResponse(TrainingEntity entity) {
        return new Training(
                entity.getId(),
                entity.getTrainingName(),
                entity.getTrainingType().getTypeName(),
                entity.getTrainingDate(),
                Duration.ofMinutes(entity.getDurationMinutes()),
                entity.getTrainee().getId(),
                entity.getTrainer().getId()
        );
    }

    public TrainingEntity toEntity(
            ScheduleTrainingRequest request,
            TraineeEntity trainee,
            TrainerEntity trainer,
            TrainingTypeEntity trainingType) {
        TrainingEntity entity = new TrainingEntity();
        entity.setTrainee(trainee);
        entity.setTrainer(trainer);
        entity.setTrainingName(request.name());
        entity.setTrainingType(trainingType);
        entity.setTrainingDate(request.date());
        entity.setDurationMinutes(Math.toIntExact(request.duration().toMinutes()));
        return entity;
    }
}
