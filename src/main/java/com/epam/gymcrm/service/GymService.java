package com.epam.gymcrm.service;

import com.epam.gymcrm.dto.Credentials;
import com.epam.gymcrm.dto.request.AutoScheduleTrainingRequest;
import com.epam.gymcrm.dto.request.ScheduleTrainingRequest;
import com.epam.gymcrm.dto.response.Trainer;
import com.epam.gymcrm.dto.response.Training;
import com.epam.gymcrm.entity.TrainerEntity;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.mapper.TrainerMapper;
import com.epam.gymcrm.mapper.TrainingMapper;
import com.epam.gymcrm.model.TrainingType;
import com.epam.gymcrm.repository.TrainerRepository;
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
    private final TrainerRepository trainerRepository;
    private final TrainerMapper trainerMapper;
    private final TrainingMapper trainingMapper;

    public Training scheduleTraining(Credentials auth, ScheduleTrainingRequest request) {
        authenticationService.requireAuthenticated(auth);

        traineeService.getActiveTrainee(request.traineeId());
        trainerService.getActiveTrainerForSpecialization(request.trainerId(), request.type());

        return trainingService.createTraining(auth, request);
    }

    public Training autoScheduleTraining(Credentials auth, AutoScheduleTrainingRequest request) {
        authenticationService.requireAuthenticated(auth);

        traineeService.getActiveTrainee(request.traineeId());
        Trainer trainer = findActiveTrainerBySpecialization(request.type());

        log.info("Auto-assigned trainer id={} for training '{}'", trainer.userId(), request.name());

        return trainingService.createTraining(
                auth, trainingMapper.toScheduleRequest(request, trainer.userId()));
    }

    public void removeTraineeProfile(Credentials auth, String username) {
        traineeService.deleteTraineeByUsername(auth, username);
        log.info("Removed trainee profile");
    }

    private Trainer findActiveTrainerBySpecialization(TrainingType typeName) {
        TrainerEntity trainer = trainerRepository.findActiveBySpecialization(typeName)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No active trainer found for type: " + typeName));
        return trainerMapper.toResponse(trainer);
    }
}
