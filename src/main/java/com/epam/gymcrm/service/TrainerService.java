package com.epam.gymcrm.service;

import com.epam.gymcrm.dto.Credentials;
import com.epam.gymcrm.dto.Trainer;
import com.epam.gymcrm.dto.request.ChangePasswordRequest;
import com.epam.gymcrm.dto.request.CreateTrainerRequest;
import com.epam.gymcrm.dto.request.ToggleActivationRequest;
import com.epam.gymcrm.dto.request.UpdateTrainerRequest;
import com.epam.gymcrm.entity.TrainerEntity;
import com.epam.gymcrm.entity.TrainingTypeEntity;
import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.mapper.TrainerMapper;
import com.epam.gymcrm.repository.TrainerRepository;
import com.epam.gymcrm.repository.TrainingTypeRepository;
import com.epam.gymcrm.util.DtoValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        log.info("Trainer profile created: id={}", created.getId());
        return trainerMapper.toResponse(created);
    }

    public Trainer updateTrainer(Credentials auth, UpdateTrainerRequest request) {
        requireTrainerAuthenticated(auth);
        dtoValidator.validate(request);

        TrainerEntity existing = trainerRepository.findById(request.id())
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found with id: " + request.id()));

        TrainingTypeEntity specialization = trainingTypeRepository.findByTypeName(request.specialization())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Training type not found: " + request.specialization()));

        TrainerEntity trainer = trainerMapper.toEntity(existing, request, specialization);
        TrainerEntity updated = trainerRepository.save(trainer);
        log.info("Trainer profile updated: id={}", updated.getId());
        return trainerMapper.toResponse(updated);
    }

    @Transactional(readOnly = true)
    public Trainer getTrainerByUsername(Credentials auth, String username) {
        requireTrainerAuthenticated(auth);

        TrainerEntity trainer = trainerRepository.findByUsername(username)
                .filter(t -> t.getUser().isActive())
                .orElseThrow(() -> {
                    log.warn("Trainer not found");
                    return new EntityNotFoundException("Trainer not found");
                });
        return trainerMapper.toResponse(trainer);
    }

    public void changePassword(Credentials auth, ChangePasswordRequest request) {
        requireTrainerAuthenticated(auth);
        userService.changePassword(request);
    }

    public void toggleActivation(Credentials auth, ToggleActivationRequest request) {
        requireTrainerAuthenticated(auth);
        userService.toggleActivation(request);
    }

    private void requireTrainerAuthenticated(Credentials auth) {
        if (!authenticationService.matchesTrainerCredentials(auth.username(), auth.password())) {
            throw new AuthenticationException("Authentication failed");
        }
    }
}
