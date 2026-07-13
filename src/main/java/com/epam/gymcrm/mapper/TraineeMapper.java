package com.epam.gymcrm.mapper;

import com.epam.gymcrm.dto.request.CreateTraineeRequest;
import com.epam.gymcrm.dto.request.UpdateTraineeRequest;
import com.epam.gymcrm.dto.response.Trainee;
import com.epam.gymcrm.entity.TraineeEntity;
import com.epam.gymcrm.entity.UserEntity;
import com.epam.gymcrm.service.PasswordGenerator;
import com.epam.gymcrm.service.UserCredentialService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TraineeMapper {

    private final UserCredentialService userCredentialService;
    private final PasswordGenerator passwordGenerator;

    public Trainee toResponse(TraineeEntity entity) {
        UserEntity user = entity.getUser();
        return new Trainee(
                entity.getId(),
                toFullName(user.getFirstName(), user.getLastName()),
                user.getUsername(),
                entity.getDateOfBirth(),
                entity.getAddress()
        );
    }

    public TraineeEntity toEntity(CreateTraineeRequest request) {
        UserEntity user = new UserEntity();
        user.setFirstName(request.user().firstName());
        user.setLastName(request.user().lastName());
        user.setUsername(userCredentialService.generateUniqueUsername(
                request.user().firstName(), request.user().lastName()));
        user.setPassword(passwordGenerator.generatePassword());
        user.setActive(true);

        TraineeEntity entity = new TraineeEntity();
        entity.setUser(user);
        entity.setDateOfBirth(request.dateOfBirth());
        entity.setAddress(request.address());
        return entity;
    }

    public TraineeEntity toEntity(UpdateTraineeRequest request, String username, String password) {
        UserEntity user = new UserEntity();
        user.setId(request.id());
        user.setFirstName(request.user().firstName());
        user.setLastName(request.user().lastName());
        user.setUsername(username);
        user.setPassword(password);
        user.setActive(request.active());

        TraineeEntity entity = new TraineeEntity();
        entity.setId(request.id());
        entity.setUser(user);
        entity.setDateOfBirth(request.dateOfBirth());
        entity.setAddress(request.address());
        return entity;
    }

    private static String toFullName(String firstName, String lastName) {
        return firstName + " " + lastName;
    }
}
