package com.epam.gymcrm.support;

import com.epam.gymcrm.mapper.TrainerMapper;
import com.epam.gymcrm.mapper.TrainingTypeMapper;
import org.mapstruct.factory.Mappers;

public final class MapperTestSupport {

    private MapperTestSupport() {
    }

    public static TrainerMapper trainerMapper() {
        TrainerMapper mapper = Mappers.getMapper(TrainerMapper.class);
        wireTrainingTypeMapper(mapper);
        return mapper;
    }

    private static void wireTrainingTypeMapper(TrainerMapper mapper) {
        try {
            var field = mapper.getClass().getDeclaredField("trainingTypeMapper");
            field.setAccessible(true);
            field.set(mapper, Mappers.getMapper(TrainingTypeMapper.class));
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to wire TrainingTypeMapper into TrainerMapper", exception);
        }
    }
}
