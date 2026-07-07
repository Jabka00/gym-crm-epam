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

@Component
@RequiredArgsConstructor
public class TrainingMapper {

    private final TraineeMapper traineeMapper;
    private final TrainerMapper trainerMapper;

    public Training toResponse(TrainingEntity entity) {
        Training response = new Training();
        response.setId(entity.getId());
        response.setTrainee(traineeMapper.toResponse(entity.getTrainee()));
        response.setTrainer(trainerMapper.toResponse(entity.getTrainer()));
        response.setTrainingName(entity.getTrainingName());
        response.setTrainingType(trainerMapper.toTrainingTypeResponse(entity.getTrainingType()));
        response.setTrainingDate(entity.getTrainingDate());
        response.setTrainingDuration(entity.getTrainingDuration());
        return response;
    }

    public TrainingEntity toEntity(
            ScheduleTrainingRequest request,
            TraineeEntity trainee,
            TrainerEntity trainer,
            TrainingTypeEntity trainingType) {
        TrainingEntity entity = new TrainingEntity();
        entity.setTrainee(trainee);
        entity.setTrainer(trainer);
        entity.setTrainingName(request.getTrainingName());
        entity.setTrainingType(trainingType);
        entity.setTrainingDate(request.getTrainingDate());
        entity.setTrainingDuration(request.getTrainingDuration());
        return entity;
    }

    public TrainingEntity toEntity(
            AutoScheduleTrainingRequest request,
            TraineeEntity trainee,
            TrainerEntity trainer,
            TrainingTypeEntity trainingType) {
        TrainingEntity entity = new TrainingEntity();
        entity.setTrainee(trainee);
        entity.setTrainer(trainer);
        entity.setTrainingName(request.getTrainingName());
        entity.setTrainingType(trainingType);
        entity.setTrainingDate(request.getTrainingDate());
        entity.setTrainingDuration(request.getTrainingDuration());
        return entity;
    }
}
