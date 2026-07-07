package com.epam.gymcrm.service;

import com.epam.gymcrm.dto.request.AutoScheduleTrainingRequest;
import com.epam.gymcrm.dto.request.ScheduleTrainingRequest;
import com.epam.gymcrm.dto.response.Trainer;
import com.epam.gymcrm.dto.response.Training;
import com.epam.gymcrm.security.Credentials;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GymService {

    private final TrainerService trainerService;
    private final TraineeService traineeService;
    private final TrainingService trainingService;
    private final AuthenticationService authenticationService;

    public Training scheduleTraining(Credentials auth, ScheduleTrainingRequest request) {
        authenticationService.requireAuthenticated(auth);

        traineeService.getActiveTrainee(request.getTraineeId());
        trainerService.getActiveTrainerForSpecialization(request.getTrainerId(), request.getTrainingType());

        return trainingService.createTraining(auth, request);
    }

    public Training autoScheduleTraining(Credentials auth, AutoScheduleTrainingRequest request) {
        authenticationService.requireAuthenticated(auth);

        traineeService.getActiveTrainee(request.getTraineeId());
        Trainer trainer = trainerService.findActiveBySpecialization(request.getTrainingType());

        log.info("Auto-assigned trainer id={} for training '{}'", trainer.getId(), request.getTrainingName());

        return trainingService.createTraining(auth, toScheduleRequest(request, trainer.getId()));
    }

    public void removeTraineeProfile(Credentials auth, String username) {
        traineeService.deleteTraineeByUsername(auth, username);
        log.info("Removed trainee profile username={}", username);
    }

    private ScheduleTrainingRequest toScheduleRequest(AutoScheduleTrainingRequest request, Long trainerId) {
        ScheduleTrainingRequest scheduleRequest = new ScheduleTrainingRequest();
        scheduleRequest.setTraineeId(request.getTraineeId());
        scheduleRequest.setTrainerId(trainerId);
        scheduleRequest.setTrainingName(request.getTrainingName());
        scheduleRequest.setTrainingType(request.getTrainingType());
        scheduleRequest.setTrainingDate(request.getTrainingDate());
        scheduleRequest.setTrainingDuration(request.getTrainingDuration());
        return scheduleRequest;
    }
}
