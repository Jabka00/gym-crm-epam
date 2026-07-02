package com.epam.gymcrm.mapper;

import com.epam.gymcrm.dto.TrainerDto;
import com.epam.gymcrm.entity.TrainerEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {TrainingTypeMapper.class})
public interface TrainerMapper {

    TrainerDto toDto(TrainerEntity trainer);

    @Mapping(target = "trainees", ignore = true)
    @Mapping(target = "trainings", ignore = true)
    TrainerEntity toEntity(TrainerDto dto);
}
