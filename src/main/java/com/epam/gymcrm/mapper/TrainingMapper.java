package com.epam.gymcrm.mapper;

import com.epam.gymcrm.dto.request.AutoScheduleTrainingRequest;
import com.epam.gymcrm.dto.request.ScheduleTrainingRequest;
import com.epam.gymcrm.dto.response.Training;
import com.epam.gymcrm.entity.TraineeEntity;
import com.epam.gymcrm.entity.TrainerEntity;
import com.epam.gymcrm.entity.TrainingEntity;
import com.epam.gymcrm.entity.TrainingTypeEntity;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class TrainingMapper {

    public TrainingEntity toEntity(ScheduleTrainingRequest request) {
        TrainingEntity training = new TrainingEntity();
        training.setTrainee(traineeRef(request.traineeId()));
        training.setTrainer(trainerRef(request.trainerId()));
        training.setTrainingName(request.name());
        training.setTrainingType(TrainingTypeEntity.of(request.type()));
        training.setTrainingDate(request.date());
        training.setTrainingDuration((int) request.duration().toMinutes());
        return training;
    }

    public TrainingEntity toEntity(AutoScheduleTrainingRequest request, Long trainerId) {
        TrainingEntity training = new TrainingEntity();
        training.setTrainee(traineeRef(request.traineeId()));
        training.setTrainer(trainerRef(trainerId));
        training.setTrainingName(request.name());
        training.setTrainingType(TrainingTypeEntity.of(request.type()));
        training.setTrainingDate(request.date());
        training.setTrainingDuration((int) request.duration().toMinutes());
        return training;
    }

    public Training toResponse(TrainingEntity training) {
        return new Training(
                training.getId(),
                training.getTrainingName(),
                training.getTrainingType().toEnum(),
                training.getTrainingDate(),
                Duration.ofMinutes(training.getTrainingDuration()),
                training.getTrainee().getId(),
                training.getTrainer().getId()
        );
    }

    private static TraineeEntity traineeRef(Long id) {
        TraineeEntity trainee = new TraineeEntity();
        trainee.setId(id);
        return trainee;
    }

    private static TrainerEntity trainerRef(Long id) {
        TrainerEntity trainer = new TrainerEntity();
        trainer.setId(id);
        return trainer;
    }
}
