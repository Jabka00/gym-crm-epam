package com.epam.gymcrm.service;

import com.epam.gymcrm.dto.Credentials;
import com.epam.gymcrm.dto.Trainee;
import com.epam.gymcrm.dto.Trainer;
import com.epam.gymcrm.dto.request.ChangePasswordRequest;
import com.epam.gymcrm.dto.request.CreateTraineeRequest;
import com.epam.gymcrm.dto.request.ToggleActivationRequest;
import com.epam.gymcrm.dto.request.UpdateTraineeRequest;
import com.epam.gymcrm.entity.TraineeEntity;
import com.epam.gymcrm.entity.TrainerEntity;
import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.exception.InvalidOperationException;
import com.epam.gymcrm.exception.ValidationException;
import com.epam.gymcrm.mapper.TraineeMapper;
import com.epam.gymcrm.mapper.TrainerMapper;
import com.epam.gymcrm.repository.TraineeRepository;
import com.epam.gymcrm.repository.TrainerRepository;
import com.epam.gymcrm.util.DtoValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TraineeService {

    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final TraineeMapper traineeMapper;
    private final TrainerMapper trainerMapper;
    private final UserService userService;
    private final AuthenticationService authenticationService;
    private final DtoValidator dtoValidator;

    public Trainee createTrainee(CreateTraineeRequest request) {
        dtoValidator.validate(request);

        TraineeEntity trainee = traineeMapper.toEntity(request);
        TraineeEntity created = traineeRepository.save(trainee);
        log.info("Trainee profile created: id={}, username={}",
                created.getId(), created.getUser().getUsername());
        return traineeMapper.toResponse(created);
    }

    public Trainee updateTrainee(Credentials auth, UpdateTraineeRequest request) {
        requireTraineeAuthenticated(auth);
        dtoValidator.validate(request);

        TraineeEntity existing = traineeRepository.findById(request.id())
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found with id: " + request.id()));

        TraineeEntity trainee = traineeMapper.toEntity(existing, request);
        TraineeEntity updated = traineeRepository.save(trainee);
        log.info("Trainee profile updated: id={}, username={}",
                updated.getId(), updated.getUser().getUsername());
        return traineeMapper.toResponse(updated);
    }

    public void deleteTraineeByUsername(Credentials auth, String username) {
        requireTraineeAuthenticated(auth);

        if (username == null || username.isBlank()) {
            throw new ValidationException("Trainee username cannot be null or empty");
        }

        traineeRepository.deleteByUsername(username);
        log.info("Trainee profile delete requested: username={}", username);
    }

    @Transactional(readOnly = true)
    public Trainee getTraineeByUsername(Credentials auth, String username) {
        requireTraineeAuthenticated(auth);

        return Optional.of(traineeRepository.findByUsername(username)
                        .orElseThrow(() -> new EntityNotFoundException("Trainee not found")))
                .filter(trainee -> trainee.getUser().isActive())
                .map(traineeMapper::toResponse)
                .orElseThrow(() -> new InvalidOperationException("Trainee is inactive"));
    }

    public void changePassword(Credentials auth, ChangePasswordRequest request) {
        requireTraineeAuthenticated(auth);
        userService.changePassword(request);
    }

    public void toggleActivation(Credentials auth, ToggleActivationRequest request) {
        requireTraineeAuthenticated(auth);
        userService.toggleActivation(request);
    }

    public void updateTrainersList(Credentials auth, String traineeUsername, Set<String> trainerUsernames) {
        requireTraineeAuthenticated(auth);

        if (traineeUsername == null || traineeUsername.isBlank()) {
            throw new ValidationException("Trainee username cannot be null or empty");
        }
        if (trainerUsernames == null) {
            throw new ValidationException("Trainer usernames set cannot be null");
        }

        TraineeEntity trainee = traineeRepository.findByUsername(traineeUsername)
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found"));

        trainee.getTrainers().clear();

        List<TrainerEntity> trainers = trainerRepository.findByUsernames(trainerUsernames);
        if (trainers.size() != trainerUsernames.size()) {
            throw new EntityNotFoundException("Trainer not found");
        }

        for (TrainerEntity trainer : trainers) {
            if (!trainer.getUser().isActive()) {
                throw new InvalidOperationException("Trainer is inactive");
            }
            trainee.getTrainers().add(trainer);
        }

        traineeRepository.save(trainee);
        log.info("Trainee trainers list updated: id={}, username={}",
                trainee.getId(), traineeUsername);
    }

    @Transactional(readOnly = true)
    public List<Trainer> getNotAssignedTrainers(Credentials auth, String traineeUsername) {
        requireTraineeAuthenticated(auth);

        if (traineeUsername == null || traineeUsername.isBlank()) {
            throw new ValidationException("Trainee username cannot be null or empty");
        }

        return trainerRepository.findNotAssignedToTrainee(traineeUsername).stream()
                .map(trainerMapper::toResponse)
                .toList();
    }

    private void requireTraineeAuthenticated(Credentials auth) {
        if (!authenticationService.matchesTraineeCredentials(auth.username(), auth.password())) {
            throw new AuthenticationException("Authentication failed");
        }
    }
}
