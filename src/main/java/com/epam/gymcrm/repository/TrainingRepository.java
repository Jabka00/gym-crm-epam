package com.epam.gymcrm.repository;

import com.epam.gymcrm.model.Training;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Repository
public class TrainingRepository {

    private final Map<Long, Training> storage = new HashMap<>();

    public Training save(Training training) {
        long id = storage.keySet().stream().mapToLong(Long::longValue).max().orElse(0L) + 1;
        training.setTrainingId(id);
        storage.put(id, training);
        log.debug("Saved training id={} name={}", id, training.getTrainingName());
        return training;
    }

    public Optional<Training> findById(Long id) {
        log.debug("findById training id={}", id);
        return Optional.ofNullable(storage.get(id));
    }

    public Collection<Training> findAll() {
        log.debug("findAll trainings, count={}", storage.size());
        return storage.values();
    }

    public void load(Collection<Training> trainings) {
        trainings.forEach(training -> storage.put(training.getTrainingId(), training));
        log.debug("Loaded {} trainings into storage", trainings.size());
    }
}
