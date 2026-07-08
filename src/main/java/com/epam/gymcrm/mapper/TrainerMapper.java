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
        return new Trainer(
                entity.getId(),
                toFullName(entity.getFirstName(), entity.getLastName()),
                entity.getUsername(),
                toTrainingTypeResponse(entity.getSpecialization())
        );
    }

    public TrainingType toTrainingTypeResponse(TrainingTypeEntity entity) {
        return new TrainingType(entity.getId(), entity.getTypeName());
    }

    public TrainerEntity toEntity(CreateTrainerRequest request, TrainingTypeEntity specialization) {
        TrainerEntity entity = new TrainerEntity();
        entity.setFirstName(request.user().firstName());
        entity.setLastName(request.user().lastName());
        entity.setSpecialization(specialization);
        entity.setUsername(userCredentialService.generateUniqueUsername(
                request.user().firstName(), request.user().lastName()));
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
        entity.setId(request.id());
        entity.setFirstName(request.user().firstName());
        entity.setLastName(request.user().lastName());
        entity.setUsername(username);
        entity.setPassword(password);
        entity.setActive(request.active());
        entity.setSpecialization(specialization);
        return entity;
    }

    private static String toFullName(String firstName, String lastName) {
        return firstName + " " + lastName;
    }
}
