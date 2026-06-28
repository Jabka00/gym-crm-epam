package com.epam.gymcrm.service;

import com.epam.gymcrm.dto.AutoScheduleTrainingRequest;
import com.epam.gymcrm.dto.ScheduleTrainingRequest;
import com.epam.gymcrm.dto.TrainerResponse;
import com.epam.gymcrm.dto.TrainingResponse;
import com.epam.gymcrm.exception.InvalidOperationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class GymService {

    private final TrainerService trainerService;
    private final TraineeService traineeService;
    private final TrainingService trainingService;

    public GymService(TrainerService trainerService,
                      TraineeService traineeService,
                      TrainingService trainingService) {
        this.trainerService = trainerService;
        this.traineeService = traineeService;
        this.trainingService = trainingService;
    }

    public TrainingResponse scheduleTraining(ScheduleTrainingRequest request) {
        traineeService.getActiveById(request.traineeId());
        trainerService.getActiveForSpecialization(request.trainerId(), request.type());
        return trainingService.schedule(request);
    }

    public TrainingResponse autoScheduleTraining(AutoScheduleTrainingRequest request) {
        traineeService.getActiveById(request.traineeId());
        TrainerResponse trainer = trainerService.findActiveBySpecialization(request.type());
        log.info("Auto-assigned trainer id={} for training '{}'", trainer.userId(), request.name());
        return trainingService.autoSchedule(request, trainer.userId());
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
}
