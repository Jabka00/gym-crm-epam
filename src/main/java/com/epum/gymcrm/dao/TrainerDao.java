package com.epum.gymcrm.dao;

import com.epum.gymcrm.model.Trainer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Repository
public class TrainerDao {

    private Map<Long, Trainer> storage;

    @Autowired
    public void setStorage(@Qualifier("trainerStorage") Map<Long, Trainer> storage) {
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
}
