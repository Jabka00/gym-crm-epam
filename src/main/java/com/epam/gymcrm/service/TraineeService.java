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
import com.epam.gymcrm.security.AuthenticationGuard;
import com.epam.gymcrm.security.Credentials;
import com.epam.gymcrm.util.UserInitializationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
    private final AuthenticationGuard authenticationGuard;

    public TraineeDto createTrainee(TraineeDto traineeDto) {
        if (traineeDto == null) {
            throw new IllegalArgumentException("Trainee cannot be null");
        }

        TraineeEntity trainee = traineeMapper.toEntity(traineeDto);
        TraineeEntity created = userInitializationUtil.createUser(trainee, traineeRepository::save, "Trainee");
        return traineeMapper.toDto(created);
    }

    public TraineeDto updateTrainee(Credentials auth, TraineeDto traineeDto) {
        authenticationGuard.ensureAuthenticated(auth);

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
    public TraineeDto getTraineeByUsername(Credentials auth, String username) {
        authenticationGuard.ensureAuthenticated(auth);

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

    public void toggleActivation(Credentials auth, String username) {
        authenticationGuard.ensureAuthenticated(auth);
        userService.toggleActivation(username);
    }

    public void updateTrainersList(Credentials auth, String traineeUsername, Set<String> trainerUsernames) {
        authenticationGuard.ensureAuthenticated(auth);

        if (traineeUsername == null || traineeUsername.isBlank()) {
            throw new IllegalArgumentException("Trainee username cannot be null or empty");
        }
        if (trainerUsernames == null) {
            throw new IllegalArgumentException("Trainer usernames set cannot be null");
        }

        TraineeEntity trainee = traineeRepository.findByUsername(traineeUsername)
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found with username: " + traineeUsername));

        trainee.getTrainers().clear();

        List<TrainerEntity> trainers = trainerRepository.findByUsernames(trainerUsernames);
        if (trainers.size() != trainerUsernames.size()) {
            Set<String> foundUsernames = trainers.stream()
                    .map(TrainerEntity::getUsername)
                    .collect(Collectors.toSet());
            String missingUsername = trainerUsernames.stream()
                    .filter(trainerUsername -> !foundUsernames.contains(trainerUsername))
                    .findFirst()
                    .orElseThrow();
            throw new EntityNotFoundException("Trainer not found with username: " + missingUsername);
        }

        for (TrainerEntity trainer : trainers) {
            if (!trainer.isActive()) {
                throw new InvalidOperationException("Trainer is inactive: username=" + trainer.getUsername());
            }
            trainee.getTrainers().add(trainer);
        }

        traineeRepository.save(trainee);
        log.info("Updated trainers list for trainee username={}", traineeUsername);
    }

    @Transactional(readOnly = true)
    public List<TrainerDto> getNotAssignedTrainers(Credentials auth, String traineeUsername) {
        authenticationGuard.ensureAuthenticated(auth);

        if (traineeUsername == null || traineeUsername.isBlank()) {
            throw new IllegalArgumentException("Trainee username cannot be null or empty");
        }

        return trainerRepository.findNotAssignedToTrainee(traineeUsername).stream()
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
