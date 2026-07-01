package com.epam.gymcrm.service;

import com.epam.gymcrm.dto.request.CreateTraineeRequest;
import com.epam.gymcrm.dto.request.UpdateTraineeRequest;
import com.epam.gymcrm.dto.response.Trainee;
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
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
public class TraineeService implements InitializingBean {

    private TraineeRepository traineeRepository;
    private UsernameGenerator usernameGenerator;
    private PasswordGenerator passwordGenerator;
    private TraineeMapper traineeMapper;

    private final AtomicLong idSequence = new AtomicLong(0);

    @Autowired
    public void setTraineeRepository(TraineeRepository traineeRepository) {
        this.traineeRepository = traineeRepository;
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
    public void setTraineeMapper(TraineeMapper traineeMapper) {
        this.traineeMapper = traineeMapper;
    }

    @Override
    public void afterPropertiesSet() {
        initIdSequence();
    }

    void initIdSequence() {
        var all = traineeRepository.findAll().toList();
        long maxId = all.stream().mapToLong(TraineeEntity::getId).max().orElse(0L);
        idSequence.set(maxId);
        log.debug("Trainee id sequence initialized to {}", maxId);
        all.stream().map(TraineeEntity::getUsername).forEach(usernameGenerator::registerExistingUsername);
    }

    public Trainee create(CreateTraineeRequest request) {
        TraineeEntity trainee = traineeMapper.toEntity(request);
        trainee.setId(idSequence.incrementAndGet());
        trainee.setUsername(usernameGenerator.generateUsername(
                trainee.getFirstName(), trainee.getLastName()));
        trainee.setPassword(passwordGenerator.generatePassword());
        trainee.setActive(true);
        TraineeEntity saved = traineeRepository.save(trainee);
        log.info("Created trainee id={}", saved.getId());
        return traineeMapper.toResponse(saved);
    }

    public Trainee update(UpdateTraineeRequest request) {
        TraineeEntity trainee = getEntity(request.userId());
        boolean nameChanged = !Objects.equals(trainee.getFirstName(), request.user().firstName())
                || !Objects.equals(trainee.getLastName(), request.user().lastName());

        traineeMapper.updateEntity(trainee, request);

        if (nameChanged) {
            trainee.setUsername(usernameGenerator.generateUsername(
                    trainee.getFirstName(), trainee.getLastName()));
            log.debug("Regenerated username for trainee id={}", trainee.getId());
        }

        TraineeEntity saved = traineeRepository.save(trainee);
        log.info("Updated trainee id={}", saved.getId());
        return traineeMapper.toResponse(saved);
    }

    public void delete(Long id) {
        getEntity(id);
        traineeRepository.delete(id);
        log.info("Deleted trainee id={}", id);
    }

    public Trainee getById(Long id) {
        return traineeMapper.toResponse(getEntity(id));
    }

    public Trainee getActiveById(Long id) {
        TraineeEntity trainee = getEntity(id);
        if (!trainee.isActive()) {
            throw new InvalidOperationException("Trainee is inactive: id=" + id);
        }
        return traineeMapper.toResponse(trainee);
    }

    public List<Trainee> findAll() {
        return traineeRepository.findAll().map(traineeMapper::toResponse).toList();
    }

    private TraineeEntity getEntity(Long id) {
        return traineeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found: id=" + id));
    }
}
