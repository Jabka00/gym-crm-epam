package com.epam.gymcrm.service;

import com.epam.gymcrm.dto.request.CreateTrainerRequest;
import com.epam.gymcrm.dto.request.UpdateTrainerRequest;
import com.epam.gymcrm.dto.response.Trainer;
import com.epam.gymcrm.entity.TrainerEntity;
import com.epam.gymcrm.entity.TrainingTypeEntity;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.exception.InvalidOperationException;
import com.epam.gymcrm.mapper.TrainerMapper;
import com.epam.gymcrm.repository.TrainerRepository;
import com.epam.gymcrm.repository.TrainingTypeRepository;

import com.epam.gymcrm.security.Credentials;
import com.epam.gymcrm.util.DtoValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TrainerService {

    private final TrainerRepository trainerRepository;
    private final TrainingTypeRepository trainingTypeRepository;
    private final TrainerMapper trainerMapper;
    private final UserService userService;
    private final AuthenticationService authenticationService;
    private final DtoValidator dtoValidator;

    public Trainer createTrainer(CreateTrainerRequest request) {
        dtoValidator.validate(request);

        TrainingTypeEntity specialization = trainingTypeRepository.findByTypeName(request.getSpecialization())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Training type not found: " + request.getSpecialization()));

        TrainerEntity trainer = trainerMapper.toEntity(request, specialization);
        TrainerEntity created = trainerRepository.save(trainer);
        return trainerMapper.toResponse(created);
    }

    @Transactional(readOnly = true)
    public boolean verifyPassword(String username, String password) {
        return authenticationService.authenticateTrainer(username, password);
    }

    public Trainer updateTrainer(Credentials auth, UpdateTrainerRequest request) {
        authenticationService.requireAuthenticated(auth);

        dtoValidator.validateForUpdate(request, UpdateTrainerRequest::getId, "Trainer");

        TrainerEntity existing = trainerRepository.findById(request.getId())
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found with id: " + request.getId()));

        TrainingTypeEntity specialization = trainingTypeRepository.findByTypeName(request.getSpecialization())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Training type not found: " + request.getSpecialization()));

        TrainerEntity trainer = trainerMapper.toEntity(
                request, specialization, existing.getUsername(), existing.getPassword());
        TrainerEntity updated = trainerRepository.save(trainer);
        log.info("Trainer profile updated successfully: {}", request.getId());
        return trainerMapper.toResponse(updated);
    }

    @Transactional(readOnly = true)
    public Trainer getTrainer(Long id) {
        return trainerMapper.toResponse(getActiveEntity(id));
    }

    @Transactional(readOnly = true)
    public List<Trainer> getAllTrainers() {
        return trainerRepository.findAll()
                .filter(TrainerEntity::isActive)
                .map(trainerMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Trainer getTrainerByUsername(Credentials auth, String username) {
        authenticationService.requireAuthenticated(auth);

        TrainerEntity trainer = trainerRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found with username: " + username));
        if (!trainer.isActive()) {
            throw new InvalidOperationException("Trainer is inactive: username=" + username);
        }
        return trainerMapper.toResponse(trainer);
    }

    public void changePassword(Credentials auth, String username, String oldPassword, String newPassword) {
        authenticationService.requireAuthenticated(auth);
        userService.changePassword(username, oldPassword, newPassword);
    }

    public void toggleActivation(Credentials auth, String username) {
        authenticationService.requireAuthenticated(auth);
        userService.toggleActivation(username);
    }

    @Transactional(readOnly = true)
    public Trainer getActiveTrainerForSpecialization(Long id, String typeName) {
        TrainerEntity trainer = getActiveEntity(id);
        if (!trainer.matchesSpecialization(typeName)) {
            throw new InvalidOperationException(
                    "Trainer specialization does not match training type: " + typeName);
        }
        return trainerMapper.toResponse(trainer);
    }

    @Transactional(readOnly = true)
    public Trainer findActiveBySpecialization(String typeName) {
        TrainerEntity trainer = trainerRepository.findActiveBySpecialization(typeName)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No active trainer found for type: " + typeName));
        return trainerMapper.toResponse(trainer);
    }

    private TrainerEntity getActiveEntity(Long id) {
        TrainerEntity trainer = trainerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found with id: " + id));
        if (!trainer.isActive()) {
            throw new InvalidOperationException("Trainer is inactive: id=" + id);
        }
        return trainer;
    }
}