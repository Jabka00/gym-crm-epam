package com.epam.gymcrm.service;

import com.epam.gymcrm.dto.TraineeDto;
import com.epam.gymcrm.dto.TrainerDto;
import com.epam.gymcrm.dto.TrainingDto;
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

    public TrainingDto scheduleTraining(Credentials auth, TrainingDto trainingDto) {
        authenticationService.requireAuthenticated(auth);

        TraineeDto trainee = traineeService.getActiveTrainee(trainingDto.getTrainee().getId());
        String typeName = trainingDto.getTrainingType().getTypeName();
        TrainerDto trainer = trainerService.getActiveTrainerForSpecialization(
                trainingDto.getTrainer().getId(), typeName);

        return trainingService.createTraining(auth, enrichTraining(trainingDto, trainee, trainer));
    }

    public TrainingDto autoScheduleTraining(Credentials auth, TrainingDto trainingDto) {
        authenticationService.requireAuthenticated(auth);

        TraineeDto trainee = traineeService.getActiveTrainee(trainingDto.getTrainee().getId());
        String typeName = trainingDto.getTrainingType().getTypeName();
        TrainerDto trainer = trainerService.findActiveBySpecialization(typeName);

        log.info("Auto-assigned trainer id={} for training '{}'", trainer.getId(), trainingDto.getTrainingName());

        return trainingService.createTraining(auth, enrichTraining(trainingDto, trainee, trainer));
    }

    public void removeTraineeProfile(Credentials auth, String username) {
        traineeService.deleteTraineeByUsername(auth, username);
        log.info("Removed trainee profile username={}", username);
    }

    private TrainingDto enrichTraining(TrainingDto source, TraineeDto trainee, TrainerDto trainer) {
        return TrainingDto.builder()
                .trainee(trainee)
                .trainer(trainer)
                .trainingName(source.getTrainingName())
                .trainingType(trainer.getSpecialization())
                .trainingDate(source.getTrainingDate())
                .trainingDuration(source.getTrainingDuration())
                .build();
    }
}
