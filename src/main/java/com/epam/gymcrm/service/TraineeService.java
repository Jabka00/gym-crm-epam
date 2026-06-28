package com.epam.gymcrm.service;

import com.epam.gymcrm.dto.CreateTraineeRequest;
import com.epam.gymcrm.dto.TraineeResponse;
import com.epam.gymcrm.dto.UpdateTraineeRequest;
import com.epam.gymcrm.entity.TraineeEntity;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.exception.InvalidOperationException;
import com.epam.gymcrm.mapper.TraineeMapper;
import com.epam.gymcrm.repository.TraineeRepository;
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
public class TraineeService implements InitializingBean {

    private TraineeRepository traineeRepository;
    private CredentialGenerator credentialGenerator;
    private TraineeMapper traineeMapper;

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

    @Autowired
    public void setTraineeMapper(TraineeMapper traineeMapper) {
        this.traineeMapper = traineeMapper;
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

    public TraineeResponse create(CreateTraineeRequest request) {
        TraineeEntity trainee = new TraineeEntity();
        trainee.setFirstName(request.user().firstName());
        trainee.setLastName(request.user().lastName());
        trainee.setDateOfBirth(request.dateOfBirth());
        trainee.setAddress(request.address());
        trainee.setUserId(idSequence.incrementAndGet());
        trainee.setUsername(credentialGenerator.generateUsername(
                trainee.getFirstName(), trainee.getLastName(), usernameCounters));
        trainee.setPassword(credentialGenerator.generatePassword());
        trainee.setActive(true);
        TraineeEntity saved = traineeRepository.save(trainee);
        log.info("Created trainee id={}", saved.getUserId());
        return traineeMapper.toResponse(saved);
    }

    public TraineeResponse update(UpdateTraineeRequest request) {
        TraineeEntity trainee = getEntity(request.userId());
        String newFirstName = request.user().firstName();
        String newLastName = request.user().lastName();
        boolean nameChanged = !Objects.equals(trainee.getFirstName(), newFirstName)
                || !Objects.equals(trainee.getLastName(), newLastName);

        trainee.setFirstName(newFirstName);
        trainee.setLastName(newLastName);
        trainee.setDateOfBirth(request.dateOfBirth());
        trainee.setAddress(request.address());
        trainee.setActive(request.active());

        if (nameChanged) {
            trainee.setUsername(credentialGenerator.generateUsername(
                    newFirstName, newLastName, usernameCounters));
            log.debug("Regenerated username for trainee id={}", trainee.getUserId());
        }

        TraineeEntity saved = traineeRepository.save(trainee);
        log.info("Updated trainee id={}", saved.getUserId());
        return traineeMapper.toResponse(saved);
    }

    public void delete(Long id) {
        getEntity(id);
        traineeRepository.delete(id);
        log.info("Deleted trainee id={}", id);
    }

    public TraineeResponse getById(Long id) {
        return traineeMapper.toResponse(getEntity(id));
    }

    public TraineeResponse getActiveById(Long id) {
        TraineeEntity trainee = getEntity(id);
        if (!trainee.isActive()) {
            throw new InvalidOperationException("Trainee is inactive: id=" + id);
        }
        return traineeMapper.toResponse(trainee);
    }

    public List<TraineeResponse> findAll() {
        return traineeRepository.findAll().map(traineeMapper::toResponse).toList();
    }

    private TraineeEntity getEntity(Long id) {
        return traineeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found: id=" + id));
    }
}
