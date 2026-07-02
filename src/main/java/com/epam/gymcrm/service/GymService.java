package com.epam.gymcrm.service;

import com.epam.gymcrm.dto.TraineeDto;
import com.epam.gymcrm.dto.TrainerDto;
import com.epam.gymcrm.dto.TrainingDto;
import com.epam.gymcrm.dto.TrainingTypeDto;
import com.epam.gymcrm.exception.InvalidOperationException;
import com.epam.gymcrm.model.TrainingType;
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

    public TrainingDto scheduleTraining(TrainingDto trainingDto) {
        validateScheduleTraining(trainingDto);

        TraineeDto trainee = traineeService.getActiveTrainee(trainingDto.getTrainee().getId());
        TrainingType trainingType = toTrainingType(trainingDto.getTrainingType());
        TrainerDto trainer = trainerService.getActiveTrainerForSpecialization(
                trainingDto.getTrainer().getId(), trainingType);

        return trainingService.createTraining(enrichTraining(trainingDto, trainee, trainer));
    }

    public TrainingDto autoScheduleTraining(TrainingDto trainingDto) {
        validateAutoScheduleTraining(trainingDto);

        TraineeDto trainee = traineeService.getActiveTrainee(trainingDto.getTrainee().getId());
        TrainingType trainingType = toTrainingType(trainingDto.getTrainingType());
        TrainerDto trainer = trainerService.findActiveBySpecialization(trainingType);

        log.info("Auto-assigned trainer id={} for training '{}'", trainer.getId(), trainingDto.getTrainingName());

        return trainingService.createTraining(enrichTraining(trainingDto, trainee, trainer));
    }

    public void removeTraineeProfile(Long traineeId) {
        traineeService.getTrainee(traineeId);
        if (trainingService.existsByTraineeId(traineeId)) {
            throw new InvalidOperationException(
                    "Cannot remove trainee id=" + traineeId + ": active trainings exist");
        }
        traineeService.deleteTrainee(traineeId);
        log.info("Removed trainee profile id={}", traineeId);
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

    private TrainingType toTrainingType(TrainingTypeDto trainingTypeDto) {
        return TrainingType.valueOf(trainingTypeDto.getTypeName());
    }

    private void validateScheduleTraining(TrainingDto trainingDto) {
        validateCommonTrainingFields(trainingDto);
        if (trainingDto.getTrainer() == null || trainingDto.getTrainer().getId() == null) {
            throw new IllegalArgumentException("Trainer id is required");
        }
    }

    private void validateAutoScheduleTraining(TrainingDto trainingDto) {
        validateCommonTrainingFields(trainingDto);
    }

    private void validateCommonTrainingFields(TrainingDto trainingDto) {
        if (trainingDto == null) {
            throw new IllegalArgumentException("Training cannot be null");
        }
        if (trainingDto.getTrainee() == null || trainingDto.getTrainee().getId() == null) {
            throw new IllegalArgumentException("Trainee id is required");
        }
        if (trainingDto.getTrainingType() == null || trainingDto.getTrainingType().getTypeName() == null) {
            throw new IllegalArgumentException("Training type is required");
        }
        if (trainingDto.getTrainingName() == null || trainingDto.getTrainingName().isBlank()) {
            throw new IllegalArgumentException("Training name is required");
        }
        if (trainingDto.getTrainingDate() == null) {
            throw new IllegalArgumentException("Training date is required");
        }
        if (trainingDto.getTrainingDuration() == null || trainingDto.getTrainingDuration() < 1) {
            throw new IllegalArgumentException("Training duration must be at least 1 minute");
        }
    }
}
