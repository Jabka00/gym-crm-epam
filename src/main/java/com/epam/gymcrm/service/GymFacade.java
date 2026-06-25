package com.epam.gymcrm.service;

import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.exception.InvalidOperationException;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.Training;
import com.epam.gymcrm.model.TrainingType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Objects;

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

    public Training scheduleTraining(Training training) {
        Trainee trainee = requireActiveTrainee(training.getTraineeId());
        Trainer trainer = requireActiveTrainer(training.getTrainerId());
        requireMatchingSpecialization(trainer, training.getTrainingType());

        log.info("Scheduling training '{}' for trainee {} with trainer {}",
                training.getTrainingName(), trainee.getUsername(), trainer.getUsername());
        return trainingService.create(training);
    }

    public Training scheduleTrainingWithAvailableTrainer(Long traineeId,
                                                         String trainingName,
                                                         TrainingType trainingType,
                                                         LocalDate trainingDate,
                                                         Duration duration) {
        requireActiveTrainee(traineeId);
        Trainer trainer = trainerService.findAll().stream()
                .filter(Trainer::isActive)
                .filter(candidate -> matchesSpecialization(candidate, trainingType))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException(
                        "No active trainer found for type: " + trainingType.trainingTypeName()));

        Training training = new Training();
        training.setTraineeId(traineeId);
        training.setTrainerId(trainer.getUserId());
        training.setTrainingName(trainingName);
        training.setTrainingType(trainingType);
        training.setTrainingDate(trainingDate);
        training.setTrainingDuration(duration);

        log.info("Auto-assigned trainer {} for training '{}'", trainer.getUsername(), trainingName);
        return trainingService.create(training);
    }

    public void removeTraineeProfile(Long traineeId) {
        requireExistingTrainee(traineeId);
        if (trainingService.existsByTraineeId(traineeId)) {
            throw new InvalidOperationException(
                    "Cannot remove trainee id=" + traineeId + ": active trainings exist");
        }
        traineeService.delete(traineeId);
        log.info("Removed trainee profile id={}", traineeId);
    }

    private Trainee requireActiveTrainee(Long traineeId) {
        Trainee trainee = requireExistingTrainee(traineeId);
        if (!trainee.isActive()) {
            throw new InvalidOperationException("Trainee is inactive: id=" + traineeId);
        }
        return trainee;
    }

    private Trainee requireExistingTrainee(Long traineeId) {
        return traineeService.findById(traineeId)
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found: id=" + traineeId));
    }

    private Trainer requireActiveTrainer(Long trainerId) {
        Trainer trainer = trainerService.findById(trainerId)
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found: id=" + trainerId));
        if (!trainer.isActive()) {
            throw new InvalidOperationException("Trainer is inactive: id=" + trainerId);
        }
        return trainer;
    }

    private void requireMatchingSpecialization(Trainer trainer, TrainingType trainingType) {
        if (!matchesSpecialization(trainer, trainingType)) {
            throw new InvalidOperationException(
                    "Trainer specialization does not match training type: "
                            + trainingType.trainingTypeName());
        }
    }

    private boolean matchesSpecialization(Trainer trainer, TrainingType trainingType) {
        return trainer.getSpecialization() != null
                && Objects.equals(
                trainer.getSpecialization().trainingTypeName(),
                trainingType.trainingTypeName());
    }
}
