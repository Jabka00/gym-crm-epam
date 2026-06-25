package com.epam.gymcrm.service;

import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.exception.InvalidOperationException;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.repository.TrainerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TrainerService {

    private TrainerRepository trainerRepository;
    private CredentialGenerator credentialGenerator;

    @Autowired
    public void setTrainerRepository(TrainerRepository trainerRepository) {
        this.trainerRepository = trainerRepository;
    }

    @Autowired
    public void setCredentialGenerator(CredentialGenerator credentialGenerator) {
        this.credentialGenerator = credentialGenerator;
    }

    public Trainer create(Trainer trainer) {
        var existingUsernames = trainerRepository.findAll().stream()
                .map(Trainer::getUsername)
                .collect(Collectors.toUnmodifiableSet());
        trainer.setUsername(credentialGenerator.generateUsername(
                trainer.getFirstName(), trainer.getLastName(), existingUsernames));
        trainer.setPassword(credentialGenerator.generatePassword());
        trainer.setActive(true);
        Trainer saved = trainerRepository.save(trainer);
        log.info("Created trainer: {}", saved.getUsername());
        return saved;
    }

    public Trainer update(Trainer trainer) {
        Trainer updated = trainerRepository.update(trainer);
        log.info("Updated trainer id={}", updated.getUserId());
        return updated;
    }

    public Optional<Trainer> findById(Long id) {
        return trainerRepository.findById(id);
    }

    public Trainer getById(Long id) {
        return findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found: id=" + id));
    }

    public Trainer getActiveById(Long id) {
        Trainer trainer = getById(id);
        if (!trainer.isActive()) {
            throw new InvalidOperationException("Trainer is inactive: id=" + id);
        }
        return trainer;
    }

    public Trainer getActiveForSpecialization(Long id, String trainingTypeName) {
        Trainer trainer = getActiveById(id);
        if (!trainer.matchesSpecialization(trainingTypeName)) {
            throw new InvalidOperationException(
                    "Trainer specialization does not match training type: " + trainingTypeName);
        }
        return trainer;
    }

    public Trainer findActiveBySpecialization(String trainingTypeName) {
        return trainerRepository.findAll().stream()
                .filter(Trainer::isActive)
                .filter(trainer -> trainer.matchesSpecialization(trainingTypeName))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException(
                        "No active trainer found for type: " + trainingTypeName));
    }

    public Collection<Trainer> findAll() {
        return trainerRepository.findAll();
    }
}
