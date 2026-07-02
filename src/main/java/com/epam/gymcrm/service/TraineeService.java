package com.epam.gymcrm.service;

import com.epam.gymcrm.dto.TraineeDto;
import com.epam.gymcrm.dto.TrainerDto;
import com.epam.gymcrm.entity.TraineeEntity;
import com.epam.gymcrm.entity.TrainerEntity;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.exception.InvalidOperationException;
import com.epam.gymcrm.mapper.TraineeMapper;
import com.epam.gymcrm.mapper.TrainerMapper;
import com.epam.gymcrm.repository.TraineeRepository;
import com.epam.gymcrm.repository.TrainerRepository;
import com.epam.gymcrm.util.UserInitializationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TraineeService {

    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final UserInitializationUtil userInitializationUtil;
    private final TraineeMapper traineeMapper;
    private final TrainerMapper trainerMapper;
    private final UserService userService;

    public TraineeDto createTrainee(TraineeDto traineeDto) {
        if (traineeDto == null) {
            throw new IllegalArgumentException("Trainee cannot be null");
        }

        TraineeEntity trainee = traineeMapper.toEntity(traineeDto);
        TraineeEntity created = userInitializationUtil.createUser(trainee, traineeRepository::save, "Trainee");
        return traineeMapper.toDto(created);
    }

    public TraineeDto updateTrainee(TraineeDto traineeDto) {
        if (traineeDto == null) {
            throw new IllegalArgumentException("Trainee cannot be null");
        }
        if (traineeDto.getId() == null) {
            throw new IllegalArgumentException("Trainee ID cannot be null");
        }

        traineeRepository.findById(traineeDto.getId())
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found with id: " + traineeDto.getId()));

        TraineeEntity trainee = traineeMapper.toEntity(traineeDto);
        TraineeEntity updated = traineeRepository.save(trainee);
        return traineeMapper.toDto(updated);
    }

    public void deleteTrainee(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Trainee ID cannot be null");
        }

        traineeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found with id: " + id));
        traineeRepository.delete(id);
        log.info("Deleted trainee id={}", id);
    }

    @Transactional(readOnly = true)
    public TraineeDto getActiveTrainee(Long id) {
        return traineeMapper.toDto(requireActiveTrainee(id));
    }

    @Transactional(readOnly = true)
    public List<TraineeDto> getAllTrainees() {
        return traineeRepository.findAll()
                .filter(TraineeEntity::isActive)
                .map(traineeMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public TraineeDto getTraineeByUsername(String username) {
        TraineeEntity trainee = traineeRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found with username: " + username));
        if (!trainee.isActive()) {
            throw new InvalidOperationException("Trainee is inactive: username=" + username);
        }
        return traineeMapper.toDto(trainee);
    }

    public void changePassword(String username, String oldPassword, String newPassword) {
        userService.changePassword(username, oldPassword, newPassword);
    }

    public void toggleActivation(String username) {
        userService.toggleActivation(username);
    }

    public void deleteTraineeByUsername(String username) {
        userService.deleteByUsername(username);
        log.info("Deleted trainee profile by username={}", username);
    }

    public void updateTrainersList(String traineeUsername, Set<String> trainerUsernames) {
        if (traineeUsername == null || traineeUsername.isBlank()) {
            throw new IllegalArgumentException("Trainee username cannot be null or empty");
        }
        if (trainerUsernames == null) {
            throw new IllegalArgumentException("Trainer usernames set cannot be null");
        }

        TraineeEntity trainee = traineeRepository.findByUsername(traineeUsername)
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found with username: " + traineeUsername));

        Set<TrainerEntity> currentTrainers = new HashSet<>(trainee.getTrainers());
        for (TrainerEntity trainer : currentTrainers) {
            trainee.getTrainers().remove(trainer);
            trainer.getTrainees().remove(trainee);
        }

        for (String trainerUsername : trainerUsernames) {
            TrainerEntity trainer = trainerRepository.findByUsername(trainerUsername)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Trainer not found with username: " + trainerUsername));
            if (!trainer.isActive()) {
                throw new InvalidOperationException("Trainer is inactive: username=" + trainerUsername);
            }

            trainee.getTrainers().add(trainer);
            trainer.getTrainees().add(trainee);
        }

        traineeRepository.save(trainee);
        log.info("Updated trainers list for trainee username={}", traineeUsername);
    }

    @Transactional(readOnly = true)
    public List<TrainerDto> getNotAssignedTrainers(String traineeUsername) {
        if (traineeUsername == null || traineeUsername.isBlank()) {
            throw new IllegalArgumentException("Trainee username cannot be null or empty");
        }

        return trainerRepository.findNotAssignedToTrainee(traineeUsername).stream()
                .filter(TrainerEntity::isActive)
                .map(trainerMapper::toDto)
                .toList();
    }

    private TraineeEntity requireActiveTrainee(Long id) {
        TraineeEntity trainee = traineeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found with id: " + id));
        if (!trainee.isActive()) {
            throw new InvalidOperationException("Trainee is inactive: id=" + id);
        }
        return trainee;
    }
}
