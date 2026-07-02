package com.epam.gymcrm.mapper;

import com.epam.gymcrm.dto.TrainingTypeDto;
import com.epam.gymcrm.entity.TrainingTypeEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TrainingTypeMapper {

    TrainingTypeDto toDto(TrainingTypeEntity trainingType);

    TrainingTypeEntity toEntity(TrainingTypeDto dto);
}
