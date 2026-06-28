package com.epam.gymcrm.service;

import com.epam.gymcrm.dto.AutoScheduleTrainingRequest;
import com.epam.gymcrm.dto.ScheduleTrainingRequest;
import com.epam.gymcrm.entity.TrainingEntity;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.repository.TrainingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
public class TrainingService implements InitializingBean {

    private TrainingRepository trainingRepository;

    private final AtomicLong idSequence = new AtomicLong(0);

    @Autowired
    public void setTrainingRepository(TrainingRepository trainingRepository) {
        this.trainingRepository = trainingRepository;
    }

    @Override
    public void afterPropertiesSet() {
        initIdSequence();
    }

    void initIdSequence() {
        long maxId = trainingRepository.findAll()
                .mapToLong(TrainingEntity::getTrainingId)
                .max().orElse(0L);
        idSequence.set(maxId);
        log.debug("Training id sequence initialized to {}", maxId);
    }

    public TrainingEntity schedule(ScheduleTrainingRequest request) {
        TrainingEntity training = new TrainingEntity();
        training.setTraineeId(request.traineeId());
        training.setTrainerId(request.trainerId());
        training.setTrainingName(request.name());
        training.setTrainingType(request.type());
        training.setTrainingDate(request.date());
        training.setTrainingDuration(request.duration());
        return create(training);
    }

    public TrainingEntity autoSchedule(AutoScheduleTrainingRequest request, Long trainerId) {
        TrainingEntity training = new TrainingEntity();
        training.setTraineeId(request.traineeId());
        training.setTrainerId(trainerId);
        training.setTrainingName(request.name());
        training.setTrainingType(request.type());
        training.setTrainingDate(request.date());
        training.setTrainingDuration(request.duration());
        return create(training);
    }

    public TrainingEntity create(TrainingEntity training) {
        training.setTrainingId(idSequence.incrementAndGet());
        TrainingEntity saved = trainingRepository.save(training);
        log.info("Created training: {} (traineeId={}, trainerId={})",
                saved.getTrainingName(), saved.getTraineeId(), saved.getTrainerId());
        return saved;
    }

    public TrainingEntity update(TrainingEntity training) {
        findById(training.getTrainingId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Training not found: id=" + training.getTrainingId()));
        TrainingEntity updated = trainingRepository.update(training);
        log.info("Updated training id={}", updated.getTrainingId());
        return updated;
    }

    public void delete(Long id) {
        findById(id).orElseThrow(() -> new EntityNotFoundException("Training not found: id=" + id));
        trainingRepository.delete(id);
        log.info("Deleted training id={}", id);
    }

    public Optional<TrainingEntity> findById(Long id) {
        return trainingRepository.findById(id);
    }

    public Collection<TrainingEntity> findAll() {
        return trainingRepository.findAll().toList();
    }

    public boolean existsByTraineeId(Long traineeId) {
        return trainingRepository.findAll()
                .anyMatch(training -> traineeId.equals(training.getTraineeId()));
    }
}
