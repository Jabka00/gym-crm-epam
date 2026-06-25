package com.epam.gymcrm.service;

import com.epam.gymcrm.dto.AutoScheduleTrainingRequest;
import com.epam.gymcrm.dto.ScheduleTrainingRequest;
import com.epam.gymcrm.dto.TrainingResponse;
import com.epam.gymcrm.exception.InvalidOperationException;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.Training;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class GymFacade {

    private final TrainerService trainerService;
    private final TraineeService traineeService;
    private final TrainingService trainingService;

    public GymFacade(TrainerService trainerService,
                     TraineeService traineeService,
                     TrainingService trainingService) {
        this.trainerService = trainerService;
        this.traineeService = traineeService;
        this.trainingService = trainingService;
    }

    public TrainingResponse scheduleTraining(ScheduleTrainingRequest request) {
        traineeService.getActiveById(request.traineeId());
        trainerService.getActiveForSpecialization(
                request.trainerId(), request.trainingType().trainingTypeName());

        Training training = new Training();
        training.setTraineeId(request.traineeId());
        training.setTrainerId(request.trainerId());
        training.setTrainingName(request.trainingName());
        training.setTrainingType(request.trainingType());
        training.setTrainingDate(request.trainingDate());
        training.setTrainingDuration(request.trainingDuration());

        return toTrainingResponse(trainingService.create(training));
    }

    public TrainingResponse autoScheduleTraining(AutoScheduleTrainingRequest request) {
        traineeService.getActiveById(request.traineeId());
        Trainer trainer = trainerService.findActiveBySpecialization(
                request.trainingType().trainingTypeName());

        Training training = new Training();
        training.setTraineeId(request.traineeId());
        training.setTrainerId(trainer.getUserId());
        training.setTrainingName(request.trainingName());
        training.setTrainingType(request.trainingType());
        training.setTrainingDate(request.trainingDate());
        training.setTrainingDuration(request.trainingDuration());

        log.info("Auto-assigned trainer {} for training '{}'", trainer.getUsername(), request.trainingName());
        return toTrainingResponse(trainingService.create(training));
    }

    public void removeTraineeProfile(Long traineeId) {
        traineeService.getById(traineeId);
        if (trainingService.existsByTraineeId(traineeId)) {
            throw new InvalidOperationException(
                    "Cannot remove trainee id=" + traineeId + ": active trainings exist");
        }
        traineeService.delete(traineeId);
        log.info("Removed trainee profile id={}", traineeId);
    }

    private TrainingResponse toTrainingResponse(Training training) {
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
