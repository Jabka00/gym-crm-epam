package com.epam.gymcrm.mapper;

import com.epam.gymcrm.dto.request.CreateTraineeRequest;
import com.epam.gymcrm.dto.request.UpdateTraineeRequest;
import com.epam.gymcrm.dto.response.Trainee;
import com.epam.gymcrm.entity.TraineeEntity;
import org.springframework.stereotype.Component;

@Component
public class TraineeMapper {

    public Trainee toResponse(TraineeEntity entity) {
        Trainee response = new Trainee();
        response.setId(entity.getId());
        response.setFirstName(entity.getFirstName());
        response.setLastName(entity.getLastName());
        response.setUsername(entity.getUsername());
        response.setPassword(entity.getPassword());
        response.setActive(entity.isActive());
        response.setDateOfBirth(entity.getDateOfBirth());
        response.setAddress(entity.getAddress());
        return response;
    }

    public TraineeEntity toEntity(CreateTraineeRequest request) {
        TraineeEntity entity = new TraineeEntity();
        entity.setFirstName(request.getFirstName());
        entity.setLastName(request.getLastName());
        entity.setDateOfBirth(request.getDateOfBirth());
        entity.setAddress(request.getAddress());
        return entity;
    }

    public TraineeEntity toEntity(UpdateTraineeRequest request, String username, String password) {
        TraineeEntity entity = new TraineeEntity();
        entity.setId(request.getId());
        entity.setFirstName(request.getFirstName());
        entity.setLastName(request.getLastName());
        entity.setUsername(username);
        entity.setPassword(password);
        entity.setActive(request.isActive());
        entity.setDateOfBirth(request.getDateOfBirth());
        entity.setAddress(request.getAddress());
        return entity;
    }
}
