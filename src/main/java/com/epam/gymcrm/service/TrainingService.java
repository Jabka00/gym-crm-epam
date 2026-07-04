package com.epam.gymcrm.service;

import com.epam.gymcrm.dto.TrainingDto;
import com.epam.gymcrm.entity.TrainingEntity;
import com.epam.gymcrm.entity.TrainingTypeEntity;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.exception.InvalidOperationException;
import com.epam.gymcrm.mapper.TrainingMapper;
import com.epam.gymcrm.repository.TraineeRepository;
import com.epam.gymcrm.repository.TrainerRepository;
import com.epam.gymcrm.repository.TrainingRepository;
import com.epam.gymcrm.repository.TrainingTypeRepository;
import com.epam.gymcrm.security.Credentials;
import com.epam.gymcrm.service.AuthenticationService;
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

    public TrainingDto createTraining(Credentials auth, TrainingDto trainingDto) {
        authenticationService.requireAuthenticated(auth);
        dtoValidator.validate(trainingDto);
        requireParticipantIds(trainingDto);

        TrainingEntity training = trainingMapper.toEntity(trainingDto);
        var trainee = traineeRepository.findById(trainingDto.getTrainee().getId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Trainee not found with id: " + trainingDto.getTrainee().getId()));
        if (!trainee.isActive()) {
            throw new InvalidOperationException(
                    "Trainee is inactive: id=" + trainingDto.getTrainee().getId());
        }
        var trainer = trainerRepository.findById(trainingDto.getTrainer().getId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Trainer not found with id: " + trainingDto.getTrainer().getId()));
        if (!trainer.isActive()) {
            throw new InvalidOperationException(
                    "Trainer is inactive: id=" + trainingDto.getTrainer().getId());
        }
        training.setTrainee(trainee);
        training.setTrainer(trainer);
        training.setTrainingType(resolveTrainingType(trainingDto));

        TrainingEntity created = trainingRepository.save(training);
        log.info("Created training with ID: {}", created.getId());
        return trainingMapper.toDto(created);
    }

    @Transactional(readOnly = true)
    public TrainingDto getTraining(Long id) {
        TrainingEntity training = trainingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Training not found with id: " + id));
        return trainingMapper.toDto(training);
    }

    @Transactional(readOnly = true)
    public List<TrainingDto> getAllTrainings() {
        return trainingRepository.findAll()
                .map(trainingMapper::toDto)
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
    public List<TrainingDto> getTraineeTrainings(
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

        List<TrainingDto> trainings = trainingRepository
                .findByTraineeUsernameAndCriteria(
                        traineeUsername, fromDate, toDate, trainerUsername, trainingTypeName)
                .stream()
                .map(trainingMapper::toDto)
                .toList();

        log.info("Found {} trainings for trainee: {}", trainings.size(), traineeUsername);
        return trainings;
    }

    @Transactional(readOnly = true)
    public List<TrainingDto> getTrainerTrainings(
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

        List<TrainingDto> trainings = trainingRepository
                .findByTrainerUsernameAndCriteria(trainerUsername, fromDate, toDate, traineeUsername)
                .stream()
                .map(trainingMapper::toDto)
                .toList();

        log.info("Found {} trainings for trainer: {}", trainings.size(), trainerUsername);
        return trainings;
    }

    private void requireParticipantIds(TrainingDto trainingDto) {
        if (trainingDto.getTrainee().getId() == null) {
            throw new IllegalArgumentException("Trainee id is required");
        }
        if (trainingDto.getTrainer() == null || trainingDto.getTrainer().getId() == null) {
            throw new IllegalArgumentException("Trainer id is required");
        }
    }

    private TrainingTypeEntity resolveTrainingType(TrainingDto trainingDto) {
        if (trainingDto.getTrainingType().getId() != null) {
            return trainingTypeRepository.findById(trainingDto.getTrainingType().getId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Training type not found with id: " + trainingDto.getTrainingType().getId()));
        }
        return trainingTypeRepository.findByTypeName(trainingDto.getTrainingType().getTypeName())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Training type not found with name: " + trainingDto.getTrainingType().getTypeName()));
    }
}
