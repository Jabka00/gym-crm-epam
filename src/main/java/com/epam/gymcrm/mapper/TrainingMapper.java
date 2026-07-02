package com.epam.gymcrm.mapper;

import com.epam.gymcrm.dto.TrainingDto;
import com.epam.gymcrm.entity.TrainingEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {TraineeMapper.class, TrainerMapper.class, TrainingTypeMapper.class})
public interface TrainingMapper {

    TrainingDto toDto(TrainingEntity training);

    TrainingEntity toEntity(TrainingDto dto);
}
