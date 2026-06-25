package com.epam.gymcrm.repository;

import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.storage.TrainerStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

@Slf4j
@Repository
public class TrainerRepository {

    private TrainerStorage storage;

    @Autowired
    public void setStorage(TrainerStorage storage) {
        this.storage = storage;
    }

    public Trainer save(Trainer trainer) {
        long id = storage.keySet().stream().mapToLong(Long::longValue).max().orElse(0L) + 1;
        trainer.setUserId(id);
        storage.put(id, trainer);
        log.debug("Saved trainer id={} username={}", id, trainer.getUsername());
        return trainer;
    }

    public Trainer update(Trainer trainer) {
        storage.put(trainer.getUserId(), trainer);
        log.debug("Updated trainer id={}", trainer.getUserId());
        return trainer;
    }

    public Optional<Trainer> findById(Long id) {
        log.debug("findById trainer id={}", id);
        return Optional.ofNullable(storage.get(id));
    }

    public Collection<Trainer> findAll() {
        log.debug("findAll trainers, count={}", storage.size());
        return storage.values();
    }

    public boolean existsById(Long id) {
        return storage.containsKey(id);
    }
}
