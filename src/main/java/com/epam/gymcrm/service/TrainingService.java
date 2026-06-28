package com.epam.gymcrm.service;

import com.epam.gymcrm.dto.AutoScheduleTrainingRequest;
import com.epam.gymcrm.dto.ScheduleTrainingRequest;
import com.epam.gymcrm.dto.TrainingResponse;
import com.epam.gymcrm.entity.TrainingEntity;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.mapper.TrainingMapper;
import com.epam.gymcrm.repository.TrainingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
public class TrainingService implements InitializingBean {

    private TrainingRepository trainingRepository;
    private TrainingMapper trainingMapper;
    private TraineeService traineeService;
    private TrainerService trainerService;

    private final AtomicLong idSequence = new AtomicLong(0);

    @Autowired
    public void setTrainingRepository(TrainingRepository trainingRepository) {
        this.trainingRepository = trainingRepository;
    }

    @Autowired
    public void setTrainingMapper(TrainingMapper trainingMapper) {
        this.trainingMapper = trainingMapper;
    }

    @Autowired
    public void setTraineeService(TraineeService traineeService) {
        this.traineeService = traineeService;
    }

    @Autowired
    public void setTrainerService(TrainerService trainerService) {
        this.trainerService = trainerService;
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

    public TrainingResponse schedule(ScheduleTrainingRequest request) {
        validateParticipants(request.traineeId(), request.trainerId());
        TrainingEntity training = new TrainingEntity();
        training.setTraineeId(request.traineeId());
        training.setTrainerId(request.trainerId());
        training.setTrainingName(request.name());
        training.setTrainingType(request.type());
        training.setTrainingDate(request.date());
        training.setTrainingDuration(request.duration());
        return trainingMapper.toResponse(save(training));
    }

    public TrainingResponse autoSchedule(AutoScheduleTrainingRequest request, Long trainerId) {
        validateParticipants(request.traineeId(), trainerId);
        TrainingEntity training = new TrainingEntity();
        training.setTraineeId(request.traineeId());
        training.setTrainerId(trainerId);
        training.setTrainingName(request.name());
        training.setTrainingType(request.type());
        training.setTrainingDate(request.date());
        training.setTrainingDuration(request.duration());
        return trainingMapper.toResponse(save(training));
    }

    public void delete(Long id) {
        getEntity(id);
        trainingRepository.delete(id);
        log.info("Deleted training id={}", id);
    }

    public TrainingResponse getById(Long id) {
        return trainingMapper.toResponse(getEntity(id));
    }

    public List<TrainingResponse> findAll() {
        return trainingRepository.findAll().map(trainingMapper::toResponse).toList();
    }

    public boolean existsByTraineeId(Long traineeId) {
        return trainingRepository.findAll()
                .anyMatch(training -> traineeId.equals(training.getTraineeId()));
    }

    private void validateParticipants(Long traineeId, Long trainerId) {
        traineeService.getById(traineeId);
        trainerService.getById(trainerId);
    }

    private TrainingEntity save(TrainingEntity training) {
        training.setTrainingId(idSequence.incrementAndGet());
        TrainingEntity saved = trainingRepository.save(training);
        log.info("Created training: {} (traineeId={}, trainerId={})",
                saved.getTrainingName(), saved.getTraineeId(), saved.getTrainerId());
        return saved;
    }

    private TrainingEntity getEntity(Long id) {
        return trainingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Training not found: id=" + id));
    }
}
