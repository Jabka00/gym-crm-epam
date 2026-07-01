package com.epam.gymcrm.repository;

import com.epam.gymcrm.entity.TrainingEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
@Repository
public class TrainingRepository {

    private Map<Long, TrainingEntity> storage;

    public void setStorage(Map<Long, TrainingEntity> storage) {
        if (this.storage != null) {
            throw new IllegalStateException("Training storage is already initialized");
        }
        this.storage = storage;
    }

    public TrainingEntity save(TrainingEntity training) {
        storage.put(training.getId(), training);
        log.debug("Saved training id={}", training.getId());
        return training;
    }

    public void delete(Long id) {
        storage.remove(id);
        log.debug("Deleted training id={}", id);
    }

    public Optional<TrainingEntity> findById(Long id) {
        log.debug("findById training id={}", id);
        return Optional.ofNullable(storage.get(id));
    }

    public Stream<TrainingEntity> findAll() {
        log.debug("findAll trainings, count={}", storage.size());
        return storage.values().stream();
    }
}
