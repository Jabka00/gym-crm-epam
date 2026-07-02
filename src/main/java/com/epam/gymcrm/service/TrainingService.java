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

    public TrainingDto createTraining(TrainingDto trainingDto) {
        validateTrainingDto(trainingDto);

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
        return trainingRepository.findAll()
                .anyMatch(training -> traineeId.equals(training.getTrainee().getId()));
    }

    @Transactional(readOnly = true)
    public List<TrainingDto> getTraineeTrainings(
            String traineeUsername,
            LocalDate fromDate,
            LocalDate toDate,
            String trainerUsername,
            Long trainingTypeId) {

        if (traineeUsername == null || traineeUsername.isBlank()) {
            throw new IllegalArgumentException("Trainee username is required");
        }

        log.info("Fetching trainings for trainee: {}, fromDate: {}, toDate: {}, trainer: {}, typeId: {}",
                traineeUsername, fromDate, toDate, trainerUsername, trainingTypeId);

        List<TrainingDto> trainings = trainingRepository
                .findByTraineeUsernameAndCriteria(
                        traineeUsername, fromDate, toDate, trainerUsername, trainingTypeId)
                .stream()
                .map(trainingMapper::toDto)
                .toList();

        log.info("Found {} trainings for trainee: {}", trainings.size(), traineeUsername);
        return trainings;
    }

    @Transactional(readOnly = true)
    public List<TrainingDto> getTrainerTrainings(
            String trainerUsername,
            LocalDate fromDate,
            LocalDate toDate,
            String traineeUsername) {

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

    private void validateTrainingDto(TrainingDto trainingDto) {
        if (trainingDto == null) {
            throw new IllegalArgumentException("Training cannot be null");
        }
        if (trainingDto.getTrainee() == null) {
            throw new IllegalArgumentException("Trainee cannot be null");
        }
        if (trainingDto.getTrainer() == null) {
            throw new IllegalArgumentException("Trainer cannot be null");
        }
        if (trainingDto.getTrainingType() == null) {
            throw new IllegalArgumentException("Training type cannot be null");
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
