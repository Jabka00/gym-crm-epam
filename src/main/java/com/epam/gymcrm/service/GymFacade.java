package com.epam.gymcrm.service;

import com.epam.gymcrm.dto.AutoScheduleTrainingRequest;
import com.epam.gymcrm.dto.ScheduleTrainingRequest;
import com.epam.gymcrm.dto.TrainingResponse;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.exception.InvalidOperationException;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.Training;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

    public TrainingResponse scheduleTraining(ScheduleTrainingRequest request) {
        Trainee trainee = requireActiveTrainee(request.traineeId());
        Trainer trainer = requireActiveTrainer(request.trainerId());
        requireMatchingSpecialization(trainer, request.trainingType().trainingTypeName());

        Training training = new Training();
        training.setTraineeId(request.traineeId());
        training.setTrainerId(request.trainerId());
        training.setTrainingName(request.trainingName());
        training.setTrainingType(request.trainingType());
        training.setTrainingDate(request.trainingDate());
        training.setTrainingDuration(request.trainingDuration());

        log.info("Scheduling training '{}' for trainee {} with trainer {}",
                request.trainingName(), trainee.getUsername(), trainer.getUsername());
        return toTrainingResponse(trainingService.create(training));
    }

    public TrainingResponse autoScheduleTraining(AutoScheduleTrainingRequest request) {
        requireActiveTrainee(request.traineeId());

        Trainer trainer = trainerService.findAll().stream()
                .filter(Trainer::isActive)
                .filter(candidate -> matchesSpecialization(candidate, request.trainingType().trainingTypeName()))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException(
                        "No active trainer found for type: " + request.trainingType().trainingTypeName()));

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
        requireExistingTrainee(traineeId);
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

    private void requireMatchingSpecialization(Trainer trainer, String trainingTypeName) {
        if (!matchesSpecialization(trainer, trainingTypeName)) {
            throw new InvalidOperationException(
                    "Trainer specialization does not match training type: " + trainingTypeName);
        }
    }

    private boolean matchesSpecialization(Trainer trainer, String trainingTypeName) {
        return trainer.getSpecialization() != null
                && Objects.equals(trainer.getSpecialization().trainingTypeName(), trainingTypeName);
    }
}
