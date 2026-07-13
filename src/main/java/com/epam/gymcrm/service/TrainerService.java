package com.epam.gymcrm.service;

import com.epam.gymcrm.dto.request.CreateTrainerRequest;
import com.epam.gymcrm.dto.request.UpdateTrainerRequest;
import com.epam.gymcrm.dto.response.Trainer;
import com.epam.gymcrm.entity.TrainerEntity;
import com.epam.gymcrm.entity.TrainingTypeEntity;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.exception.InvalidOperationException;
import com.epam.gymcrm.mapper.TrainerMapper;
import com.epam.gymcrm.model.TrainingType;
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

        TrainingTypeEntity specialization = trainingTypeRepository.findByTypeName(request.specialization())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Training type not found: " + request.specialization()));

        TrainerEntity trainer = trainerMapper.toEntity(request, specialization);
        TrainerEntity created = trainerRepository.save(trainer);
        return trainerMapper.toResponse(created);
    }

    public Trainer updateTrainer(Credentials auth, UpdateTrainerRequest request) {
        authenticationService.requireAuthenticated(auth);

        dtoValidator.validateForUpdate(request, UpdateTrainerRequest::id, "Trainer");

        TrainerEntity existing = trainerRepository.findById(request.id())
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found with id: " + request.id()));

        TrainingTypeEntity specialization = trainingTypeRepository.findByTypeName(request.specialization())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Training type not found: " + request.specialization()));

        TrainerEntity trainer = trainerMapper.toEntity(
                request, specialization, existing.getUser().getUsername(), existing.getUser().getPassword());
        TrainerEntity updated = trainerRepository.save(trainer);
        log.info("Trainer profile updated successfully: {}", request.id());
        return trainerMapper.toResponse(updated);
    }

    @Transactional(readOnly = true)
    public Trainer getTrainer(Long id) {
        return trainerMapper.toResponse(getActiveEntity(id));
    }

    @Transactional(readOnly = true)
    public List<Trainer> getAllTrainers() {
        return trainerRepository.findAll()
                .filter(trainer -> trainer.getUser().isActive())
                .map(trainerMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Trainer getTrainerByUsername(Credentials auth, String username) {
        authenticationService.requireAuthenticated(auth);

        TrainerEntity trainer = trainerRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found with username: " + username));
        if (!trainer.getUser().isActive()) {
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
    public Trainer getActiveTrainerForSpecialization(Long id, TrainingType typeName) {
        TrainerEntity trainer = getActiveEntity(id);
        if (!matchesSpecialization(trainer, typeName)) {
            throw new InvalidOperationException(
                    "Trainer specialization does not match training type: " + typeName);
        }
        return trainerMapper.toResponse(trainer);
    }

    private TrainerEntity getActiveEntity(Long id) {
        TrainerEntity trainer = trainerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found with id: " + id));
        if (!trainer.getUser().isActive()) {
            throw new InvalidOperationException("Trainer is inactive: id=" + id);
        }
        return trainer;
    }

    private static boolean matchesSpecialization(TrainerEntity trainer, TrainingType type) {
        return type != null
                && trainer.getSpecialization() != null
                && type == trainer.getSpecialization().getTypeName();
    }
}
