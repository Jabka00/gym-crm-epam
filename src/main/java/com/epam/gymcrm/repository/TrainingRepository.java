package com.epam.gymcrm.repository;

import com.epam.gymcrm.model.Training;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Repository
public class TrainingRepository {

    private Map<Long, Training> storage;

    public void setStorage(Map<Long, Training> storage) {
        if (this.storage != null) {
            throw new IllegalStateException("Training storage is already initialized");
        }
        this.storage = storage;
    }

    public Training save(Training training) {
        long id = storage.keySet().stream().mapToLong(Long::longValue).max().orElse(0L) + 1;
        training.setTrainingId(id);
        storage.put(id, training);
        log.debug("Saved training id={} name={}", id, training.getTrainingName());
        return training;
    }

    public Training update(Training training) {
        storage.put(training.getTrainingId(), training);
        log.debug("Updated training id={}", training.getTrainingId());
        return training;
    }

    public void delete(Long id) {
        storage.remove(id);
        log.debug("Deleted training id={}", id);
    }

    public Optional<Training> findById(Long id) {
        log.debug("findById training id={}", id);
        return Optional.ofNullable(storage.get(id));
    }

    public Collection<Training> findAll() {
        log.debug("findAll trainings, count={}", storage.size());
        return storage.values();
    }
}
