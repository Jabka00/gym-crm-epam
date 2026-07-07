package com.epam.gymcrm.mapper;

import com.epam.gymcrm.dto.request.CreateTrainerRequest;
import com.epam.gymcrm.dto.request.UpdateTrainerRequest;
import com.epam.gymcrm.dto.response.Trainer;
import com.epam.gymcrm.dto.response.TrainingType;
import com.epam.gymcrm.entity.TrainerEntity;
import com.epam.gymcrm.entity.TrainingTypeEntity;
import com.epam.gymcrm.service.UserCredentialService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TrainerMapper {

    private final UserCredentialService userCredentialService;

    public Trainer toResponse(TrainerEntity entity) {
        Trainer response = new Trainer();
        response.setId(entity.getId());
        response.setFirstName(entity.getFirstName());
        response.setLastName(entity.getLastName());
        response.setUsername(entity.getUsername());
        response.setPassword(entity.getPassword());
        response.setActive(entity.isActive());
        response.setSpecialization(toTrainingTypeResponse(entity.getSpecialization()));
        return response;
    }

    public TrainingType toTrainingTypeResponse(TrainingTypeEntity entity) {
        TrainingType response = new TrainingType();
        response.setId(entity.getId());
        response.setTypeName(entity.getTypeName());
        return response;
    }

    public TrainerEntity toEntity(CreateTrainerRequest request, TrainingTypeEntity specialization) {
        TrainerEntity entity = new TrainerEntity();
        entity.setFirstName(request.getFirstName());
        entity.setLastName(request.getLastName());
        entity.setSpecialization(specialization);
        entity.setUsername(userCredentialService.generateUniqueUsername(
                request.getFirstName(), request.getLastName()));
        entity.setPassword(userCredentialService.generatePassword());
        entity.setActive(true);
        return entity;
    }

    public TrainerEntity toEntity(
            UpdateTrainerRequest request,
            TrainingTypeEntity specialization,
            String username,
            String password) {
        TrainerEntity entity = new TrainerEntity();
        entity.setId(request.getId());
        entity.setFirstName(request.getFirstName());
        entity.setLastName(request.getLastName());
        entity.setUsername(username);
        entity.setPassword(password);
        entity.setActive(request.isActive());
        entity.setSpecialization(specialization);
        return entity;
    }
}
