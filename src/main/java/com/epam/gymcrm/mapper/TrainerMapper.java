package com.epam.gymcrm.mapper;

import com.epam.gymcrm.dto.request.CreateTrainerRequest;
import com.epam.gymcrm.dto.request.UpdateTrainerRequest;
import com.epam.gymcrm.dto.response.Trainer;
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

    public Trainer toResponse(TrainerEntity trainer) {
        return new Trainer(
                trainer.getUserId(),
                trainer.getFirstName() + " " + trainer.getLastName(),
                trainer.getUsername(),
                trainer.getSpecialization()
        );
    }
}
