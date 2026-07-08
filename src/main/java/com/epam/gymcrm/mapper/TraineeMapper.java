package com.epam.gymcrm.mapper;

import com.epam.gymcrm.dto.request.CreateTraineeRequest;
import com.epam.gymcrm.dto.request.UpdateTraineeRequest;
import com.epam.gymcrm.dto.response.Trainee;
import com.epam.gymcrm.entity.TraineeEntity;
import com.epam.gymcrm.service.UserCredentialService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TraineeMapper {

    private final UserCredentialService userCredentialService;

    public Trainee toResponse(TraineeEntity entity) {
        return new Trainee(
                entity.getId(),
                toFullName(entity.getFirstName(), entity.getLastName()),
                entity.getUsername(),
                entity.getDateOfBirth(),
                entity.getAddress()
        );
    }

    public TraineeEntity toEntity(CreateTraineeRequest request) {
        TraineeEntity entity = new TraineeEntity();
        entity.setFirstName(request.user().firstName());
        entity.setLastName(request.user().lastName());
        entity.setDateOfBirth(request.dateOfBirth());
        entity.setAddress(request.address());
        entity.setUsername(userCredentialService.generateUniqueUsername(
                request.user().firstName(), request.user().lastName()));
        entity.setPassword(userCredentialService.generatePassword());
        entity.setActive(true);
        return entity;
    }

    public TraineeEntity toEntity(UpdateTraineeRequest request, String username, String password) {
        TraineeEntity entity = new TraineeEntity();
        entity.setId(request.id());
        entity.setFirstName(request.user().firstName());
        entity.setLastName(request.user().lastName());
        entity.setUsername(username);
        entity.setPassword(password);
        entity.setActive(request.active());
        entity.setDateOfBirth(request.dateOfBirth());
        entity.setAddress(request.address());
        return entity;
    }

    private static String toFullName(String firstName, String lastName) {
        return firstName + " " + lastName;
    }
}
