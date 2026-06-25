package com.epam.gymcrm.service;

import com.epam.gymcrm.dto.AutoScheduleTrainingRequest;
import com.epam.gymcrm.dto.CreateTraineeRequest;
import com.epam.gymcrm.dto.CreateTrainerRequest;
import com.epam.gymcrm.dto.ScheduleTrainingRequest;
import com.epam.gymcrm.dto.TraineeResponse;
import com.epam.gymcrm.dto.TrainerResponse;
import com.epam.gymcrm.dto.TrainingResponse;
import com.epam.gymcrm.dto.UpdateTraineeRequest;
import com.epam.gymcrm.dto.UpdateTrainerRequest;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.exception.InvalidOperationException;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.Training;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

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


    public TraineeResponse createTrainee(CreateTraineeRequest request) {
        Trainee trainee = new Trainee();
        trainee.setFirstName(request.firstName());
        trainee.setLastName(request.lastName());
        trainee.setDateOfBirth(request.dateOfBirth());
        trainee.setAddress(request.address());
        return toTraineeResponse(traineeService.create(trainee));
    }

    public TraineeResponse updateTrainee(UpdateTraineeRequest request) {
        Trainee trainee = new Trainee();
        trainee.setUserId(request.userId());
        trainee.setFirstName(request.firstName());
        trainee.setLastName(request.lastName());
        trainee.setDateOfBirth(request.dateOfBirth());
        trainee.setAddress(request.address());
        trainee.setActive(request.active());
        return toTraineeResponse(traineeService.update(trainee));
    }

    public void deleteTrainee(Long id) {
        traineeService.delete(id);
    }

    public Optional<TraineeResponse> findTrainee(Long id) {
        return traineeService.findById(id).map(this::toTraineeResponse);
    }

    public Collection<TraineeResponse> findAllTrainees() {
        return traineeService.findAll().stream()
                .map(this::toTraineeResponse)
                .toList();
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

    public TrainerResponse createTrainer(CreateTrainerRequest request) {
        Trainer trainer = new Trainer();
        trainer.setFirstName(request.firstName());
        trainer.setLastName(request.lastName());
        trainer.setSpecialization(request.specialization());
        return toTrainerResponse(trainerService.create(trainer));
    }

    public TrainerResponse updateTrainer(UpdateTrainerRequest request) {
        Trainer trainer = new Trainer();
        trainer.setUserId(request.userId());
        trainer.setFirstName(request.firstName());
        trainer.setLastName(request.lastName());
        trainer.setSpecialization(request.specialization());
        trainer.setActive(request.active());
        return toTrainerResponse(trainerService.update(trainer));
    }

    public Optional<TrainerResponse> findTrainer(Long id) {
        return trainerService.findById(id).map(this::toTrainerResponse);
    }

    public Collection<TrainerResponse> findAllTrainers() {
        return trainerService.findAll().stream()
                .map(this::toTrainerResponse)
                .toList();
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

    public Optional<TrainingResponse> findTraining(Long id) {
        return trainingService.findById(id).map(this::toTrainingResponse);
    }

    private TraineeResponse toTraineeResponse(Trainee trainee) {
        return new TraineeResponse(
                trainee.getUserId(),
                trainee.getFirstName(),
                trainee.getLastName(),
                trainee.getUsername(),
                trainee.isActive(),
                trainee.getDateOfBirth(),
                trainee.getAddress()
        );
    }

    private TrainerResponse toTrainerResponse(Trainer trainer) {
        return new TrainerResponse(
                trainer.getUserId(),
                trainer.getFirstName(),
                trainer.getLastName(),
                trainer.getUsername(),
                trainer.isActive(),
                trainer.getSpecialization()
        );
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
