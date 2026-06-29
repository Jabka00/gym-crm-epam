package com.epam.gymcrm.mapper;

import com.epam.gymcrm.dto.AutoScheduleTrainingRequest;
import com.epam.gymcrm.dto.ScheduleTrainingRequest;
import com.epam.gymcrm.dto.TrainingResponse;
import com.epam.gymcrm.entity.TrainingEntity;
import org.springframework.stereotype.Component;

@Component
public class TrainingMapper {

    public TrainingEntity toEntity(ScheduleTrainingRequest request) {
        TrainingEntity training = new TrainingEntity();
        training.setTraineeId(request.traineeId());
        training.setTrainerId(request.trainerId());
        training.setTrainingName(request.name());
        training.setTrainingType(request.type());
        training.setTrainingDate(request.date());
        training.setTrainingDuration(request.duration());
        return training;
    }

    public TrainingEntity toEntity(AutoScheduleTrainingRequest request, Long trainerId) {
        TrainingEntity training = new TrainingEntity();
        training.setTraineeId(request.traineeId());
        training.setTrainerId(trainerId);
        training.setTrainingName(request.name());
        training.setTrainingType(request.type());
        training.setTrainingDate(request.date());
        training.setTrainingDuration(request.duration());
        return training;
    }

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
