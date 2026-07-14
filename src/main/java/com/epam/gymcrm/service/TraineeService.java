package com.epam.gymcrm.service;

import com.epam.gymcrm.dto.Credentials;
import com.epam.gymcrm.dto.request.ChangePasswordRequest;
import com.epam.gymcrm.dto.request.CreateTraineeRequest;
import com.epam.gymcrm.dto.request.ToggleActivationRequest;
import com.epam.gymcrm.dto.request.UpdateTraineeRequest;
import com.epam.gymcrm.dto.response.Trainee;
import com.epam.gymcrm.dto.response.Trainer;
import com.epam.gymcrm.entity.TraineeEntity;
import com.epam.gymcrm.entity.TrainerEntity;
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
import java.util.Set;
import java.util.stream.Collectors;

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
        return traineeMapper.toResponse(created);
    }

    public Trainee updateTrainee(Credentials auth, UpdateTraineeRequest request) {
        authenticationService.requireAuthenticated(auth);

        dtoValidator.validateForUpdate(request, UpdateTraineeRequest::id, "Trainee");

        TraineeEntity existing = traineeRepository.findById(request.id())
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found with id: " + request.id()));

        TraineeEntity trainee = traineeMapper.toEntity(
                request, existing.getUser().getUsername(), existing.getUser().getPassword());
        TraineeEntity updated = traineeRepository.save(trainee);
        return traineeMapper.toResponse(updated);
    }

    public void deleteTraineeByUsername(Credentials auth, String username) {
        authenticationService.requireAuthenticated(auth);

        if (username == null || username.isBlank()) {
            throw new ValidationException("Trainee username cannot be null or empty");
        }

        traineeRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found with username: " + username));

        traineeRepository.deleteByUsername(username);
        log.info("Deleted trainee");
    }

    @Transactional(readOnly = true)
    public Trainee getActiveTrainee(Long id) {
        return traineeMapper.toResponse(requireActiveTrainee(id));
    }

    @Transactional(readOnly = true)
    public List<Trainee> getAllTrainees() {
        return traineeRepository.findAll()
                .filter(trainee -> trainee.getUser().isActive())
                .map(traineeMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Trainee getTraineeByUsername(Credentials auth, String username) {
        authenticationService.requireAuthenticated(auth);

        TraineeEntity trainee = traineeRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found with username: " + username));
        if (!trainee.getUser().isActive()) {
            throw new InvalidOperationException("Trainee is inactive: username=" + username);
        }
        return traineeMapper.toResponse(trainee);
    }

    public void changePassword(Credentials auth, ChangePasswordRequest request) {
        authenticationService.requireAuthenticated(auth);
        userService.changePassword(request);
    }

    public void toggleActivation(Credentials auth, ToggleActivationRequest request) {
        authenticationService.requireAuthenticated(auth);
        userService.toggleActivation(request);
    }

    public void updateTrainersList(Credentials auth, String traineeUsername, Set<String> trainerUsernames) {
        authenticationService.requireAuthenticated(auth);

        if (traineeUsername == null || traineeUsername.isBlank()) {
            throw new ValidationException("Trainee username cannot be null or empty");
        }
        if (trainerUsernames == null) {
            throw new ValidationException("Trainer usernames set cannot be null");
        }

        TraineeEntity trainee = traineeRepository.findByUsername(traineeUsername)
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found with username: " + traineeUsername));

        trainee.getTrainers().clear();

        List<TrainerEntity> trainers = trainerRepository.findByUsernames(trainerUsernames);
        if (trainers.size() != trainerUsernames.size()) {
            Set<String> foundUsernames = trainers.stream()
                    .map(trainer -> trainer.getUser().getUsername())
                    .collect(Collectors.toSet());
            String missingUsername = trainerUsernames.stream()
                    .filter(trainerUsername -> !foundUsernames.contains(trainerUsername))
                    .findFirst()
                    .orElseThrow();
            throw new EntityNotFoundException("Trainer not found with username: " + missingUsername);
        }

        for (TrainerEntity trainer : trainers) {
            if (!trainer.getUser().isActive()) {
                throw new InvalidOperationException(
                        "Trainer is inactive: username=" + trainer.getUser().getUsername());
            }
            trainee.getTrainers().add(trainer);
        }

        traineeRepository.save(trainee);
        log.info("Updated trainers list for trainee");
    }

    @Transactional(readOnly = true)
    public List<Trainer> getNotAssignedTrainers(Credentials auth, String traineeUsername) {
        authenticationService.requireAuthenticated(auth);

        if (traineeUsername == null || traineeUsername.isBlank()) {
            throw new ValidationException("Trainee username cannot be null or empty");
        }

        return trainerRepository.findNotAssignedToTrainee(traineeUsername).stream()
                .map(trainerMapper::toResponse)
                .toList();
    }

    private TraineeEntity requireActiveTrainee(Long id) {
        TraineeEntity trainee = traineeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found with id: " + id));
        if (!trainee.getUser().isActive()) {
            throw new InvalidOperationException("Trainee is inactive: id=" + id);
        }
        return trainee;
    }
}
