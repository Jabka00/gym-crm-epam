package com.epam.gymcrm.mapper;

import com.epam.gymcrm.dto.Trainee;
import com.epam.gymcrm.dto.request.CreateTraineeRequest;
import com.epam.gymcrm.dto.request.UpdateTraineeRequest;
import com.epam.gymcrm.entity.TraineeEntity;
import com.epam.gymcrm.entity.UserEntity;
import com.epam.gymcrm.service.PasswordGenerator;
import com.epam.gymcrm.service.UsernameGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TraineeMapper {

    private final UsernameGenerator usernameGenerator;
    private final PasswordGenerator passwordGenerator;

    public Trainee toResponse(TraineeEntity entity) {
        UserEntity user = entity.getUser();
        return new Trainee(
                entity.getId(),
                user.getFirstName() + " " + user.getLastName(),
                user.getUsername(),
                entity.getDateOfBirth(),
                entity.getAddress()
        );
    }

    public TraineeEntity toEntity(CreateTraineeRequest request) {
        UserEntity user = new UserEntity();
        user.setFirstName(request.user().firstName());
        user.setLastName(request.user().lastName());
        user.setUsername(usernameGenerator.generateUniqueUsername(
                request.user().firstName(), request.user().lastName()));
        user.setPassword(passwordGenerator.generatePassword());
        user.setActive(true);

        TraineeEntity entity = new TraineeEntity();
        entity.setUser(user);
        entity.setDateOfBirth(request.dateOfBirth());
        entity.setAddress(request.address());
        return entity;
    }

    public TraineeEntity toEntity(TraineeEntity existing, UpdateTraineeRequest request) {
        UserEntity user = existing.getUser();
        user.setFirstName(request.user().firstName());
        user.setLastName(request.user().lastName());
        existing.setDateOfBirth(request.dateOfBirth());
        existing.setAddress(request.address());
        return existing;
    }
}
