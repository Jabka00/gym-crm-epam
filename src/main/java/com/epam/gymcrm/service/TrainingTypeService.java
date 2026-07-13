package com.epam.gymcrm.service;

import com.epam.gymcrm.dto.response.TrainingTypeResponse;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.mapper.TrainerMapper;
import com.epam.gymcrm.model.TrainingType;
import com.epam.gymcrm.repository.TrainingTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TrainingTypeService {

    private final TrainingTypeRepository trainingTypeRepository;
    private final TrainerMapper trainerMapper;

    public TrainingTypeResponse getTrainingTypeByName(TrainingType typeName) {
        return trainingTypeRepository.findByTypeName(typeName)
                .map(trainerMapper::toTrainingTypeResponse)
                .orElseThrow(() -> new EntityNotFoundException("Training type not found: " + typeName));
    }
}
