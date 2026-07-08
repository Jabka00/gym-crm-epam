package com.epam.gymcrm.service;

import com.epam.gymcrm.dto.request.ScheduleTrainingRequest;
import com.epam.gymcrm.dto.response.Training;
import com.epam.gymcrm.entity.TraineeEntity;
import com.epam.gymcrm.entity.TrainerEntity;
import com.epam.gymcrm.entity.TrainingEntity;
import com.epam.gymcrm.entity.TrainingTypeEntity;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.mapper.TrainingMapper;
import com.epam.gymcrm.repository.TraineeRepository;
import com.epam.gymcrm.repository.TrainerRepository;
import com.epam.gymcrm.repository.TrainingRepository;
import com.epam.gymcrm.repository.TrainingTypeRepository;
import com.epam.gymcrm.security.Credentials;
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
        authenticationService.requireAuthenticated(auth);
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
        log.info("Created training with ID: {}", created.getId());
        return trainingMapper.toResponse(created);
    }

    @Transactional(readOnly = true)
    public Training getTraining(Long id) {
        TrainingEntity training = trainingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Training not found with id: " + id));
        return trainingMapper.toResponse(training);
    }

    @Transactional(readOnly = true)
    public List<Training> getAllTrainings() {
        return trainingRepository.findAll()
                .map(trainingMapper::toResponse)
                .toList();
    }

    public void deleteTraining(Long id) {
        trainingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Training not found with id: " + id));
        trainingRepository.delete(id);
        log.info("Deleted training id={}", id);
    }

    @Transactional(readOnly = true)
    public boolean existsByTraineeId(Long traineeId) {
        return trainingRepository.existsByTraineeId(traineeId);
    }

    @Transactional(readOnly = true)
    public List<Training> getTraineeTrainings(
            Credentials auth,
            String traineeUsername,
            LocalDate fromDate,
            LocalDate toDate,
            String trainerUsername,
            String trainingTypeName) {

        authenticationService.requireAuthenticated(auth);

        if (traineeUsername == null || traineeUsername.isBlank()) {
            throw new IllegalArgumentException("Trainee username is required");
        }

        log.info("Fetching trainings for trainee: {}, fromDate: {}, toDate: {}, trainer: {}, typeName: {}",
                traineeUsername, fromDate, toDate, trainerUsername, trainingTypeName);

        List<Training> trainings = trainingRepository
                .findByTraineeUsernameAndCriteria(
                        traineeUsername, fromDate, toDate, trainerUsername, trainingTypeName)
                .stream()
                .map(trainingMapper::toResponse)
                .toList();

        log.info("Found {} trainings for trainee: {}", trainings.size(), traineeUsername);
        return trainings;
    }

    @Transactional(readOnly = true)
    public List<Training> getTrainerTrainings(
            Credentials auth,
            String trainerUsername,
            LocalDate fromDate,
            LocalDate toDate,
            String traineeUsername) {

        authenticationService.requireAuthenticated(auth);

        if (trainerUsername == null || trainerUsername.isBlank()) {
            throw new IllegalArgumentException("Trainer username is required");
        }

        log.info("Fetching trainings for trainer: {}, fromDate: {}, toDate: {}, trainee: {}",
                trainerUsername, fromDate, toDate, traineeUsername);

        List<Training> trainings = trainingRepository
                .findByTrainerUsernameAndCriteria(trainerUsername, fromDate, toDate, traineeUsername)
                .stream()
                .map(trainingMapper::toResponse)
                .toList();

        log.info("Found {} trainings for trainer: {}", trainings.size(), trainerUsername);
        return trainings;
    }
}
