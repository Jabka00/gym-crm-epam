package com.epam.gymcrm.service;

import com.epam.gymcrm.entity.TraineeEntity;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.exception.InvalidOperationException;
import com.epam.gymcrm.repository.TraineeRepository;
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
public class TraineeService implements InitializingBean {

    private TraineeRepository traineeRepository;
    private CredentialGenerator credentialGenerator;

    private static final Pattern SUFFIX_PATTERN = Pattern.compile("^(.*?)(\\d+)$");

    private final AtomicLong idSequence = new AtomicLong(0);
    private final ConcurrentHashMap<String, AtomicInteger> usernameCounters = new ConcurrentHashMap<>();

    @Autowired
    public void setTraineeRepository(TraineeRepository traineeRepository) {
        this.traineeRepository = traineeRepository;
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
        var all = traineeRepository.findAll().toList();
        long maxId = all.stream().mapToLong(TraineeEntity::getUserId).max().orElse(0L);
        idSequence.set(maxId);
        log.debug("Trainee id sequence initialized to {}", maxId);
        all.stream().map(TraineeEntity::getUsername).forEach(this::registerUsername);
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

    public TraineeEntity create(TraineeEntity trainee) {
        trainee.setUserId(idSequence.incrementAndGet());
        trainee.setUsername(credentialGenerator.generateUsername(
                trainee.getFirstName(), trainee.getLastName(), usernameCounters));
        trainee.setPassword(credentialGenerator.generatePassword());
        trainee.setActive(true);
        TraineeEntity saved = traineeRepository.save(trainee);
        log.info("Created trainee id={}", saved.getUserId());
        return saved;
    }

    public TraineeEntity update(TraineeEntity trainee) {
        findById(trainee.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found: id=" + trainee.getUserId()));
        TraineeEntity updated = traineeRepository.update(trainee);
        log.info("Updated trainee id={}", updated.getUserId());
        return updated;
    }

    public void delete(Long id) {
        findById(id).orElseThrow(() -> new EntityNotFoundException("Trainee not found: id=" + id));
        traineeRepository.delete(id);
        log.info("Deleted trainee id={}", id);
    }

    public Optional<TraineeEntity> findById(Long id) {
        return traineeRepository.findById(id);
    }

    public TraineeEntity getById(Long id) {
        return findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found: id=" + id));
    }

    public TraineeEntity getActiveById(Long id) {
        TraineeEntity trainee = getById(id);
        if (!trainee.isActive()) {
            throw new InvalidOperationException("Trainee is inactive: id=" + id);
        }
        return trainee;
    }

    public Collection<TraineeEntity> findAll() {
        return traineeRepository.findAll().toList();
    }
}
