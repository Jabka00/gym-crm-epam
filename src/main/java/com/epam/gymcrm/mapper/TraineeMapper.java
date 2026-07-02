package com.epam.gymcrm.mapper;

import com.epam.gymcrm.dto.TraineeDto;
import com.epam.gymcrm.entity.TraineeEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TraineeMapper {

    TraineeDto toDto(TraineeEntity trainee);

    @Mapping(target = "trainers", ignore = true)
    @Mapping(target = "trainings", ignore = true)
    TraineeEntity toEntity(TraineeDto dto);
}
