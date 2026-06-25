package com.epam.gymcrm.service;

import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.repository.TraineeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TraineeService {

    private TraineeRepository traineeRepository;
    private CredentialGenerator credentialGenerator;

    @Autowired
    public void setTraineeRepository(TraineeRepository traineeRepository) {
        this.traineeRepository = traineeRepository;
    }

    @Autowired
    public void setCredentialGenerator(CredentialGenerator credentialGenerator) {
        this.credentialGenerator = credentialGenerator;
    }

    public Trainee create(Trainee trainee) {
        var existingUsernames = traineeRepository.findAll().stream()
                .map(Trainee::getUsername)
                .collect(Collectors.toUnmodifiableSet());
        trainee.setUsername(credentialGenerator.generateUsername(
                trainee.getFirstName(), trainee.getLastName(), existingUsernames));
        trainee.setPassword(credentialGenerator.generatePassword());
        trainee.setActive(true);
        Trainee saved = traineeRepository.save(trainee);
        log.info("Created trainee: {}", saved.getUsername());
        return saved;
    }

    public Trainee update(Trainee trainee) {
        findById(trainee.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found: id=" + trainee.getUserId()));
        Trainee updated = traineeRepository.update(trainee);
        log.info("Updated trainee id={}", updated.getUserId());
        return updated;
    }

    public void delete(Long id) {
        findById(id).orElseThrow(() -> new EntityNotFoundException("Trainee not found: id=" + id));
        traineeRepository.delete(id);
        log.info("Deleted trainee id={}", id);
    }

    public Optional<Trainee> findById(Long id) {
        return traineeRepository.findById(id);
    }

    public Collection<Trainee> findAll() {
        return traineeRepository.findAll();
    }
}
