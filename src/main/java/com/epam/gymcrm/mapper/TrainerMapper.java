package com.epam.gymcrm.mapper;

import com.epam.gymcrm.dto.request.CreateTrainerRequest;
import com.epam.gymcrm.dto.request.UpdateTrainerRequest;
import com.epam.gymcrm.dto.response.Trainer;
import com.epam.gymcrm.dto.response.TrainingTypeResponse;
import com.epam.gymcrm.entity.TrainerEntity;
import com.epam.gymcrm.entity.TrainingTypeEntity;
import com.epam.gymcrm.entity.UserEntity;
import com.epam.gymcrm.service.PasswordGenerator;
import com.epam.gymcrm.service.UserCredentialService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TrainerMapper {

    private final UserCredentialService userCredentialService;
    private final PasswordGenerator passwordGenerator;

    public Trainer toResponse(TrainerEntity entity) {
        UserEntity user = entity.getUser();
        return new Trainer(
                entity.getId(),
                user.getFirstName() + " " + user.getLastName(),
                user.getUsername(),
                toTrainingTypeResponse(entity.getSpecialization())
        );
    }

    public TrainingTypeResponse toTrainingTypeResponse(TrainingTypeEntity entity) {
        return new TrainingTypeResponse(entity.getId(), entity.getTypeName());
    }

    public TrainerEntity toEntity(CreateTrainerRequest request, TrainingTypeEntity specialization) {
        UserEntity user = new UserEntity();
        user.setFirstName(request.user().firstName());
        user.setLastName(request.user().lastName());
        user.setUsername(userCredentialService.generateUniqueUsername(
                request.user().firstName(), request.user().lastName()));
        user.setPassword(passwordGenerator.generatePassword());
        user.setActive(true);

        TrainerEntity entity = new TrainerEntity();
        entity.setUser(user);
        entity.setSpecialization(specialization);
        return entity;
    }

    public TrainerEntity toEntity(
            UpdateTrainerRequest request,
            TrainingTypeEntity specialization,
            String username,
            String password) {
        UserEntity user = new UserEntity();
        user.setId(request.id());
        user.setFirstName(request.user().firstName());
        user.setLastName(request.user().lastName());
        user.setUsername(username);
        user.setPassword(password);
        user.setActive(request.active());

        TrainerEntity entity = new TrainerEntity();
        entity.setId(request.id());
        entity.setUser(user);
        entity.setSpecialization(specialization);
        return entity;
    }
}
