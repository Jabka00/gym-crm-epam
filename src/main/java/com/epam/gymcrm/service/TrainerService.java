package com.epam.gymcrm.service;

import com.epam.gymcrm.dto.CreateTrainerRequest;
import com.epam.gymcrm.dto.TrainerResponse;
import com.epam.gymcrm.dto.UpdateTrainerRequest;
import com.epam.gymcrm.entity.TrainerEntity;
import com.epam.gymcrm.entity.TrainingType;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class TrainerService implements InitializingBean {

    private TrainerRepository trainerRepository;
    private CredentialGenerator credentialGenerator;
    private TrainerMapper trainerMapper;

    private static final Pattern SUFFIX_PATTERN = Pattern.compile("^(.*?)(\\d+)$");

    private final AtomicLong idSequence = new AtomicLong(0);
    private final ConcurrentHashMap<String, AtomicInteger> usernameCounters = new ConcurrentHashMap<>();

    @Autowired
    public void setTrainerRepository(TrainerRepository trainerRepository) {
        this.trainerRepository = trainerRepository;
    }

    @Autowired
    public void setCredentialGenerator(CredentialGenerator credentialGenerator) {
        this.credentialGenerator = credentialGenerator;
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
        all.stream().map(TrainerEntity::getUsername).forEach(this::registerUsername);
    }

    private void registerUsername(String username) {
        Matcher m = SUFFIX_PATTERN.matcher(username);
        if (m.matches()) {
            int needed = Integer.parseInt(m.group(2)) + 1;
            usernameCounters.compute(m.group(1), (k, v) -> {
                if (v == null) return new AtomicInteger(needed);
                v.updateAndGet(cur -> Math.max(cur, needed));
                return v;
            });
        } else {
            usernameCounters.compute(username, (k, v) -> {
                if (v == null) return new AtomicInteger(1);
                v.updateAndGet(cur -> Math.max(cur, 1));
                return v;
            });
        }
    }

    public TrainerResponse create(CreateTrainerRequest request) {
        TrainerEntity trainer = new TrainerEntity();
        trainer.setFirstName(request.user().firstName());
        trainer.setLastName(request.user().lastName());
        trainer.setSpecialization(request.specialization());
        trainer.setUserId(idSequence.incrementAndGet());
        trainer.setUsername(credentialGenerator.generateUsername(
                trainer.getFirstName(), trainer.getLastName(), usernameCounters));
        trainer.setPassword(credentialGenerator.generatePassword());
        trainer.setActive(true);
        TrainerEntity saved = trainerRepository.save(trainer);
        log.info("Created trainer id={}", saved.getUserId());
        return trainerMapper.toResponse(saved);
    }

    public TrainerResponse update(UpdateTrainerRequest request) {
        TrainerEntity trainer = getEntity(request.userId());
        String newFirstName = request.user().firstName();
        String newLastName = request.user().lastName();
        boolean nameChanged = !Objects.equals(trainer.getFirstName(), newFirstName)
                || !Objects.equals(trainer.getLastName(), newLastName);

        trainer.setFirstName(newFirstName);
        trainer.setLastName(newLastName);
        trainer.setSpecialization(request.specialization());
        trainer.setActive(request.active());

        if (nameChanged) {
            trainer.setUsername(credentialGenerator.generateUsername(
                    newFirstName, newLastName, usernameCounters));
            log.debug("Regenerated username for trainer id={}", trainer.getUserId());
        }

        TrainerEntity saved = trainerRepository.save(trainer);
        log.info("Updated trainer id={}", saved.getUserId());
        return trainerMapper.toResponse(saved);
    }

    public TrainerResponse getById(Long id) {
        return trainerMapper.toResponse(getEntity(id));
    }

    public TrainerResponse getActiveById(Long id) {
        return trainerMapper.toResponse(getActiveEntity(id));
    }

    public TrainerResponse getActiveForSpecialization(Long id, TrainingType type) {
        TrainerEntity trainer = getActiveEntity(id);
        if (!trainer.matchesSpecialization(type)) {
            throw new InvalidOperationException(
                    "Trainer specialization does not match training type: " + type);
        }
        return trainerMapper.toResponse(trainer);
    }

    public TrainerResponse findActiveBySpecialization(TrainingType type) {
        TrainerEntity trainer = trainerRepository.findAll()
                .filter(TrainerEntity::isActive)
                .filter(t -> t.matchesSpecialization(type))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException(
                        "No active trainer found for type: " + type));
        return trainerMapper.toResponse(trainer);
    }

    public List<TrainerResponse> findAll() {
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
