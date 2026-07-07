package com.epam.gymcrm.service;

import com.epam.gymcrm.dto.response.TrainingType;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.mapper.TrainerMapper;
import com.epam.gymcrm.repository.TrainingTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainingTypeService {

    private final TrainingTypeRepository trainingTypeRepository;
    private final TrainerMapper trainerMapper;

    @Transactional(readOnly = true)
    public TrainingType getTrainingTypeByName(String typeName) {
        return trainingTypeRepository.findByTypeName(typeName)
                .map(trainerMapper::toTrainingTypeResponse)
                .orElseThrow(() -> new EntityNotFoundException("Training type not found: " + typeName));
    }
}
