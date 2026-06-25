package com.epam.gymcrm.repository;

import com.epam.gymcrm.model.Trainee;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Repository
public class TraineeRepository {

    private Map<Long, Trainee> storage;

    public void setStorage(Map<Long, Trainee> storage) {
        if (this.storage != null) {
            throw new IllegalStateException("Trainee storage is already initialized");
        }
        this.storage = storage;
    }

    public Trainee save(Trainee trainee) {
        long id = storage.keySet().stream().mapToLong(Long::longValue).max().orElse(0L) + 1;
        trainee.setUserId(id);
        storage.put(id, trainee);
        log.debug("Saved trainee id={} username={}", id, trainee.getUsername());
        return trainee;
    }

    public Trainee update(Trainee trainee) {
        storage.put(trainee.getUserId(), trainee);
        log.debug("Updated trainee id={}", trainee.getUserId());
        return trainee;
    }

    public void delete(Long id) {
        storage.remove(id);
        log.debug("Deleted trainee id={}", id);
    }

    public Optional<Trainee> findById(Long id) {
        log.debug("findById trainee id={}", id);
        return Optional.ofNullable(storage.get(id));
    }

    public Collection<Trainee> findAll() {
        log.debug("findAll trainees, count={}", storage.size());
        return storage.values();
    }

    public boolean existsById(Long id) {
        return storage.containsKey(id);
    }
}
