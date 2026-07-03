package com.epam.gymcrm.service;

import com.epam.gymcrm.dto.TrainingTypeDto;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.mapper.TrainingTypeMapper;
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
    private final TrainingTypeMapper trainingTypeMapper;

    @Transactional(readOnly = true)
    public TrainingTypeDto getTrainingTypeByName(String typeName) {
        return trainingTypeRepository.findByTypeName(typeName)
                .map(trainingTypeMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Training type not found: " + typeName));
    }
}
