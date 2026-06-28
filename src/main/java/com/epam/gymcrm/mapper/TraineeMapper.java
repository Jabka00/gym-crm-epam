package com.epam.gymcrm.mapper;

import com.epam.gymcrm.dto.TraineeResponse;
import com.epam.gymcrm.entity.TraineeEntity;
import org.springframework.stereotype.Component;

@Component
public class TraineeMapper {

    public TraineeResponse toResponse(TraineeEntity trainee) {
        return new TraineeResponse(
                trainee.getUserId(),
                trainee.getUsername(),
                trainee.getDateOfBirth(),
                trainee.getAddress()
        );
    }
}
