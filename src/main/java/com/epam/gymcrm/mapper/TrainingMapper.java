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

import java.time.Duration;
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
                Duration.ofMinutes(entity.getDurationMinutes()),
                entity.getTrainee().getId(),
                entity.getTrainer().getId()
        );
    }

    public ScheduleTrainingRequest toScheduleRequest(AutoScheduleTrainingRequest request, Long trainerId) {
        return new ScheduleTrainingRequest(
                request.traineeId(),
                trainerId,
                request.name(),
                request.type(),
                request.date(),
                request.duration()
        );
    }

    public TrainingEntity toEntity(
            ScheduleTrainingRequest request,
            TraineeEntity trainee,
            TrainerEntity trainer,
            TrainingTypeEntity trainingType) {
        return buildEntity(trainee, trainer, trainingType,
                request.name(), request.date(), request.duration());
    }

    public TrainingEntity toEntity(
            AutoScheduleTrainingRequest request,
            TraineeEntity trainee,
            TrainerEntity trainer,
            TrainingTypeEntity trainingType) {
        return buildEntity(trainee, trainer, trainingType,
                request.name(), request.date(), request.duration());
    }

    private static TrainingEntity buildEntity(
            TraineeEntity trainee,
            TrainerEntity trainer,
            TrainingTypeEntity trainingType,
            String name,
            LocalDate date,
            Duration duration) {
        TrainingEntity entity = new TrainingEntity();
        entity.setTrainee(trainee);
        entity.setTrainer(trainer);
        entity.setTrainingName(name);
        entity.setTrainingType(trainingType);
        entity.setTrainingDate(date);
        entity.setDurationMinutes(Math.toIntExact(duration.toMinutes()));
        return entity;
    }
}
