package com.epam.gymcrm.service;

import com.epam.gymcrm.model.Training;
import com.epam.gymcrm.repository.TrainingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;

@Slf4j
@Service
public class TrainingService {

    private TrainingRepository trainingRepository;

    @Autowired
    public void setTrainingRepository(TrainingRepository trainingRepository) {
        this.trainingRepository = trainingRepository;
    }

    public Training create(Training training) {
        Training saved = trainingRepository.save(training);
        log.info("Created training: {} (traineeId={}, trainerId={})",
                saved.getTrainingName(), saved.getTraineeId(), saved.getTrainerId());
        return saved;
    }

    public Optional<Training> findById(Long id) {
        return trainingRepository.findById(id);
    }

    public Collection<Training> findAll() {
        return trainingRepository.findAll();
    }

    public boolean existsByTraineeId(Long traineeId) {
        return trainingRepository.findAll().stream()
                .anyMatch(training -> traineeId.equals(training.getTraineeId()));
    }
}
