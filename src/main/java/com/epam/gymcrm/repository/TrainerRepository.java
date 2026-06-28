package com.epam.gymcrm.repository;

import com.epam.gymcrm.entity.TrainerEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
@Repository
public class TrainerRepository {

    private Map<Long, TrainerEntity> storage;

    public void setStorage(Map<Long, TrainerEntity> storage) {
        if (this.storage != null) {
            throw new IllegalStateException("Trainer storage is already initialized");
        }
        this.storage = storage;
    }

    public TrainerEntity save(TrainerEntity trainer) {
        storage.put(trainer.getUserId(), trainer);
        log.debug("Saved trainer id={}", trainer.getUserId());
        return trainer;
    }

    public TrainerEntity update(TrainerEntity trainer) {
        storage.put(trainer.getUserId(), trainer);
        log.debug("Updated trainer id={}", trainer.getUserId());
        return trainer;
    }

    public Optional<TrainerEntity> findById(Long id) {
        log.debug("findById trainer id={}", id);
        return Optional.ofNullable(storage.get(id));
    }

    public Stream<TrainerEntity> findAll() {
        log.debug("findAll trainers, count={}", storage.size());
        return storage.values().stream();
    }

    public boolean existsById(Long id) {
        return storage.containsKey(id);
    }
}
