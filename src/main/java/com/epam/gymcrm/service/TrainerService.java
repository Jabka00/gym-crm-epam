package com.epam.gymcrm.service;

import com.epam.gymcrm.entity.TrainerEntity;
import com.epam.gymcrm.entity.TrainingType;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.exception.InvalidOperationException;
import com.epam.gymcrm.repository.TrainerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;
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

    public TrainerEntity create(TrainerEntity trainer) {
        trainer.setUserId(idSequence.incrementAndGet());
        trainer.setUsername(credentialGenerator.generateUsername(
                trainer.getFirstName(), trainer.getLastName(), usernameCounters));
        trainer.setPassword(credentialGenerator.generatePassword());
        trainer.setActive(true);
        TrainerEntity saved = trainerRepository.save(trainer);
        log.info("Created trainer id={}", saved.getUserId());
        return saved;
    }

    public TrainerEntity update(TrainerEntity trainer) {
        TrainerEntity updated = trainerRepository.update(trainer);
        log.info("Updated trainer id={}", updated.getUserId());
        return updated;
    }

    public Optional<TrainerEntity> findById(Long id) {
        return trainerRepository.findById(id);
    }

    public TrainerEntity getById(Long id) {
        return findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found: id=" + id));
    }

    public TrainerEntity getActiveById(Long id) {
        TrainerEntity trainer = getById(id);
        if (!trainer.isActive()) {
            throw new InvalidOperationException("Trainer is inactive: id=" + id);
        }
        return trainer;
    }

    public TrainerEntity getActiveForSpecialization(Long id, TrainingType type) {
        TrainerEntity trainer = getActiveById(id);
        if (!trainer.matchesSpecialization(type)) {
            throw new InvalidOperationException(
                    "Trainer specialization does not match training type: " + type);
        }
        return trainer;
    }

    public TrainerEntity findActiveBySpecialization(TrainingType type) {
        return trainerRepository.findAll()
                .filter(TrainerEntity::isActive)
                .filter(trainer -> trainer.matchesSpecialization(type))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException(
                        "No active trainer found for type: " + type));
    }

    public Collection<TrainerEntity> findAll() {
        return trainerRepository.findAll().toList();
    }
}
