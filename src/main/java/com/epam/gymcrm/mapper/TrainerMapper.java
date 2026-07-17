package com.epam.gymcrm.mapper;

import com.epam.gymcrm.dto.Trainer;
import com.epam.gymcrm.dto.request.CreateTrainerRequest;
import com.epam.gymcrm.dto.request.UpdateTrainerRequest;
import com.epam.gymcrm.entity.TrainerEntity;
import com.epam.gymcrm.entity.TrainingTypeEntity;
import com.epam.gymcrm.entity.UserEntity;
import com.epam.gymcrm.service.PasswordGenerator;
import com.epam.gymcrm.service.UsernameGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TrainerMapper {

    private final UsernameGenerator usernameGenerator;
    private final PasswordGenerator passwordGenerator;

    public Trainer toResponse(TrainerEntity entity) {
        UserEntity user = entity.getUser();
        return new Trainer(
                entity.getId(),
                user.getFirstName() + " " + user.getLastName(),
                user.getUsername(),
                entity.getSpecialization().getTypeName()
        );
    }

    public TrainerEntity toEntity(CreateTrainerRequest request, TrainingTypeEntity specialization) {
        UserEntity user = new UserEntity();
        user.setFirstName(request.user().firstName());
        user.setLastName(request.user().lastName());
        user.setUsername(usernameGenerator.generateUniqueUsername(
                request.user().firstName(), request.user().lastName()));
        user.setPassword(passwordGenerator.generatePassword());
        user.setActive(true);

        TrainerEntity entity = new TrainerEntity();
        entity.setUser(user);
        entity.setSpecialization(specialization);
        return entity;
    }

    public TrainerEntity toEntity(
            TrainerEntity existing,
            UpdateTrainerRequest request,
            TrainingTypeEntity specialization) {
        UserEntity user = existing.getUser();
        user.setFirstName(request.user().firstName());
        user.setLastName(request.user().lastName());
        existing.setSpecialization(specialization);
        return existing;
    }
}
