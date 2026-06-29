package com.epam.gymcrm.mapper;

import com.epam.gymcrm.dto.CreateTraineeRequest;
import com.epam.gymcrm.dto.TraineeResponse;
import com.epam.gymcrm.dto.UpdateTraineeRequest;
import com.epam.gymcrm.entity.TraineeEntity;
import org.springframework.stereotype.Component;

@Component
public class TraineeMapper {

    public TraineeEntity toEntity(CreateTraineeRequest request) {
        TraineeEntity trainee = new TraineeEntity();
        trainee.setFirstName(request.user().firstName());
        trainee.setLastName(request.user().lastName());
        trainee.setDateOfBirth(request.dateOfBirth());
        trainee.setAddress(request.address());
        return trainee;
    }

    public void updateEntity(TraineeEntity trainee, UpdateTraineeRequest request) {
        trainee.setFirstName(request.user().firstName());
        trainee.setLastName(request.user().lastName());
        trainee.setDateOfBirth(request.dateOfBirth());
        trainee.setAddress(request.address());
        trainee.setActive(request.active());
    }

    public TraineeResponse toResponse(TraineeEntity trainee) {
        return new TraineeResponse(
                trainee.getUserId(),
                trainee.getFirstName() + " " + trainee.getLastName(),
                trainee.getUsername(),
                trainee.getDateOfBirth(),
                trainee.getAddress()
        );
    }
}
