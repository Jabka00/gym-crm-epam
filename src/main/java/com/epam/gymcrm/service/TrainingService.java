package com.epam.gymcrm.service;

import com.epam.gymcrm.dto.Credentials;
import com.epam.gymcrm.dto.Training;
import com.epam.gymcrm.dto.request.ScheduleTrainingRequest;
import com.epam.gymcrm.entity.TraineeEntity;
import com.epam.gymcrm.entity.TrainerEntity;
import com.epam.gymcrm.entity.TrainingEntity;
import com.epam.gymcrm.entity.TrainingTypeEntity;
import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.exception.ValidationException;
import com.epam.gymcrm.mapper.TrainingMapper;
import com.epam.gymcrm.model.TrainingType;
import com.epam.gymcrm.repository.TraineeRepository;
import com.epam.gymcrm.repository.TrainerRepository;
import com.epam.gymcrm.repository.TrainingRepository;
import com.epam.gymcrm.repository.TrainingTypeRepository;
import com.epam.gymcrm.util.DtoValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TrainingService {

    private final TrainingRepository trainingRepository;
    private final TrainingTypeRepository trainingTypeRepository;
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final TrainingMapper trainingMapper;
    private final AuthenticationService authenticationService;
    private final DtoValidator dtoValidator;

    public Training createTraining(Credentials auth, ScheduleTrainingRequest request) {
        if (!authenticationService.matchesTraineeCredentials(auth.username(), auth.password())) {
            throw new AuthenticationException("Authentication failed");
        }

        dtoValidator.validate(request);

        TraineeEntity trainee = traineeRepository.findById(request.traineeId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Trainee not found with id: " + request.traineeId()));
        TrainerEntity trainer = trainerRepository.findById(request.trainerId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Trainer not found with id: " + request.trainerId()));
        TrainingTypeEntity trainingType = trainingTypeRepository.findByTypeName(request.type())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Training type not found: " + request.type()));

        TrainingEntity training = trainingMapper.toEntity(request, trainee, trainer, trainingType);
        TrainingEntity created = trainingRepository.save(training);
        log.info("Training created");
        return trainingMapper.toResponse(created);
    }

    @Transactional(readOnly = true)
    public List<Training> getTraineeTrainings(
            Credentials auth,
            String traineeUsername,
            LocalDate fromDate,
            LocalDate toDate,
            String trainerUsername,
            TrainingType trainingTypeName) {

        if (!authenticationService.matchesTraineeCredentials(auth.username(), auth.password())) {
            throw new AuthenticationException("Authentication failed");
        }

        if (traineeUsername == null || traineeUsername.isBlank()) {
            throw new ValidationException("Trainee username is required");
        }

        List<Training> trainings = trainingRepository
                .findByTraineeUsernameAndCriteria(
                        traineeUsername, fromDate, toDate, trainerUsername, trainingTypeName)
                .stream()
                .map(trainingMapper::toResponse)
                .toList();

        log.info("Fetched trainee trainings, count={}", trainings.size());
        return trainings;
    }

    @Transactional(readOnly = true)
    public List<Training> getTrainerTrainings(
            Credentials auth,
            String trainerUsername,
            LocalDate fromDate,
            LocalDate toDate,
            String traineeUsername) {

        if (!authenticationService.matchesTrainerCredentials(auth.username(), auth.password())) {
            throw new AuthenticationException("Authentication failed");
        }

        if (trainerUsername == null || trainerUsername.isBlank()) {
            throw new ValidationException("Trainer username is required");
        }

        List<Training> trainings = trainingRepository
                .findByTrainerUsernameAndCriteria(trainerUsername, fromDate, toDate, traineeUsername)
                .stream()
                .map(trainingMapper::toResponse)
                .toList();

        log.info("Fetched trainer trainings, count={}", trainings.size());
        return trainings;
    }
}
