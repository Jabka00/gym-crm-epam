package com.epam.gymcrm.mapper;

import com.epam.gymcrm.dto.request.CreateTraineeRequest;
import com.epam.gymcrm.dto.request.UpdateTraineeRequest;
import com.epam.gymcrm.dto.response.Trainee;
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

    public Trainee toResponse(TraineeEntity trainee) {
        return new Trainee(
                trainee.getUserId(),
                trainee.getFirstName() + " " + trainee.getLastName(),
                trainee.getUsername(),
                trainee.getDateOfBirth(),
                trainee.getAddress()
        );
    }
}
