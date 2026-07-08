package com.epam.gymcrm.mapper;

import com.epam.gymcrm.dto.request.AutoScheduleTrainingRequest;
import com.epam.gymcrm.dto.request.ScheduleTrainingRequest;
import com.epam.gymcrm.dto.response.Training;
import com.epam.gymcrm.entity.TraineeEntity;
import com.epam.gymcrm.entity.TrainerEntity;
import com.epam.gymcrm.entity.TrainingEntity;
import com.epam.gymcrm.entity.TrainingTypeEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class TrainingMapper {

    private final TrainerMapper trainerMapper;

    public Training toResponse(TrainingEntity entity) {
        return new Training(
                entity.getId(),
                entity.getTrainingName(),
                trainerMapper.toTrainingTypeResponse(entity.getTrainingType()),
                entity.getTrainingDate(),
                entity.getTrainingDuration(),
                entity.getTrainee().getId(),
                entity.getTrainer().getId()
        );
    }

    public TrainingEntity toEntity(
            ScheduleTrainingRequest request,
            TraineeEntity trainee,
            TrainerEntity trainer,
            TrainingTypeEntity trainingType) {
        return buildEntity(trainee, trainer, trainingType,
                request.trainingName(), request.trainingDate(), request.trainingDuration());
    }

    public TrainingEntity toEntity(
            AutoScheduleTrainingRequest request,
            TraineeEntity trainee,
            TrainerEntity trainer,
            TrainingTypeEntity trainingType) {
        return buildEntity(trainee, trainer, trainingType,
                request.trainingName(), request.trainingDate(), request.trainingDuration());
    }

    private static TrainingEntity buildEntity(
            TraineeEntity trainee,
            TrainerEntity trainer,
            TrainingTypeEntity trainingType,
            String trainingName,
            LocalDate trainingDate,
            Integer trainingDuration) {
        TrainingEntity entity = new TrainingEntity();
        entity.setTrainee(trainee);
        entity.setTrainer(trainer);
        entity.setTrainingName(trainingName);
        entity.setTrainingType(trainingType);
        entity.setTrainingDate(trainingDate);
        entity.setTrainingDuration(trainingDuration);
        return entity;
    }
}
