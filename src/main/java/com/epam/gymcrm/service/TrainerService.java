package com.epam.gymcrm.service;

import com.epam.gymcrm.dto.request.CreateTrainerRequest;
import com.epam.gymcrm.dto.request.UpdateTrainerRequest;
import com.epam.gymcrm.dto.response.Trainer;
import com.epam.gymcrm.entity.TrainerEntity;
import com.epam.gymcrm.model.TrainingType;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.exception.InvalidOperationException;
import com.epam.gymcrm.mapper.TrainerMapper;
import com.epam.gymcrm.repository.TrainerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
public class TrainerService implements InitializingBean {

    private TrainerRepository trainerRepository;
    private UsernameGenerator usernameGenerator;
    private PasswordGenerator passwordGenerator;
    private TrainerMapper trainerMapper;

    private final AtomicLong idSequence = new AtomicLong(0);

    @Autowired
    public void setTrainerRepository(TrainerRepository trainerRepository) {
        this.trainerRepository = trainerRepository;
    }

    @Autowired
    public void setUsernameGenerator(UsernameGenerator usernameGenerator) {
        this.usernameGenerator = usernameGenerator;
    }

    @Autowired
    public void setPasswordGenerator(PasswordGenerator passwordGenerator) {
        this.passwordGenerator = passwordGenerator;
    }

    @Autowired
    public void setTrainerMapper(TrainerMapper trainerMapper) {
        this.trainerMapper = trainerMapper;
    }

    @Override
    public void afterPropertiesSet() {
        initIdSequence();
    }

    void initIdSequence() {
        var all = trainerRepository.findAll().toList();
        long maxId = all.stream().mapToLong(TrainerEntity::getUserId).max().orElse(0L);
        idSequence.set(maxId);
        log.debug("Trainer id sequence initialized to {}", maxId);
        all.stream().map(TrainerEntity::getUsername).forEach(usernameGenerator::registerExistingUsername);
    }

    public Trainer create(CreateTrainerRequest request) {
        TrainerEntity trainer = trainerMapper.toEntity(request);
        trainer.setUserId(idSequence.incrementAndGet());
        trainer.setUsername(usernameGenerator.generateUsername(
                trainer.getFirstName(), trainer.getLastName()));
        trainer.setPassword(passwordGenerator.generatePassword());
        trainer.setActive(true);
        TrainerEntity saved = trainerRepository.save(trainer);
        log.info("Created trainer id={}", saved.getUserId());
        return trainerMapper.toResponse(saved);
    }

    public Trainer update(UpdateTrainerRequest request) {
        TrainerEntity trainer = getEntity(request.userId());
        boolean nameChanged = !Objects.equals(trainer.getFirstName(), request.user().firstName())
                || !Objects.equals(trainer.getLastName(), request.user().lastName());

        trainerMapper.updateEntity(trainer, request);

        if (nameChanged) {
            trainer.setUsername(usernameGenerator.generateUsername(
                    trainer.getFirstName(), trainer.getLastName()));
            log.debug("Regenerated username for trainer id={}", trainer.getUserId());
        }

        TrainerEntity saved = trainerRepository.save(trainer);
        log.info("Updated trainer id={}", saved.getUserId());
        return trainerMapper.toResponse(saved);
    }

    public Trainer getById(Long id) {
        return trainerMapper.toResponse(getEntity(id));
    }

    public Trainer getActiveById(Long id) {
        return trainerMapper.toResponse(getActiveEntity(id));
    }

    public Trainer getActiveForSpecialization(Long id, TrainingType type) {
        TrainerEntity trainer = getActiveEntity(id);
        if (!trainer.matchesSpecialization(type)) {
            throw new InvalidOperationException(
                    "Trainer specialization does not match training type: " + type);
        }
        return trainerMapper.toResponse(trainer);
    }

    public Trainer findActiveBySpecialization(TrainingType type) {
        TrainerEntity trainer = trainerRepository.findAll()
                .filter(TrainerEntity::isActive)
                .filter(t -> t.matchesSpecialization(type))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException(
                        "No active trainer found for type: " + type));
        return trainerMapper.toResponse(trainer);
    }

    public List<Trainer> findAll() {
        return trainerRepository.findAll().map(trainerMapper::toResponse).toList();
    }

    private TrainerEntity getActiveEntity(Long id) {
        TrainerEntity trainer = getEntity(id);
        if (!trainer.isActive()) {
            throw new InvalidOperationException("Trainer is inactive: id=" + id);
        }
        return trainer;
    }

    private TrainerEntity getEntity(Long id) {
        return trainerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found: id=" + id));
    }
}
