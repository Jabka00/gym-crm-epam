package com.epam.gymcrm.mapper;

import com.epam.gymcrm.dto.CreateTrainerRequest;
import com.epam.gymcrm.dto.TrainerResponse;
import com.epam.gymcrm.dto.UpdateTrainerRequest;
import com.epam.gymcrm.entity.TrainerEntity;
import org.springframework.stereotype.Component;

@Component
public class TrainerMapper {

    public TrainerEntity toEntity(CreateTrainerRequest request) {
        TrainerEntity trainer = new TrainerEntity();
        trainer.setFirstName(request.user().firstName());
        trainer.setLastName(request.user().lastName());
        trainer.setSpecialization(request.specialization());
        return trainer;
    }

    public void updateEntity(TrainerEntity trainer, UpdateTrainerRequest request) {
        trainer.setFirstName(request.user().firstName());
        trainer.setLastName(request.user().lastName());
        trainer.setSpecialization(request.specialization());
        trainer.setActive(request.active());
    }

    public TrainerResponse toResponse(TrainerEntity trainer) {
        return new TrainerResponse(
                trainer.getUserId(),
                trainer.getFirstName() + " " + trainer.getLastName(),
                trainer.getUsername(),
                trainer.getSpecialization()
        );
    }
}
