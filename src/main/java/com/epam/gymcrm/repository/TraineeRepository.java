package com.epam.gymcrm.repository;

import com.epam.gymcrm.entity.TraineeEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
@Repository
public class TraineeRepository {

    private Map<Long, TraineeEntity> storage;

    public void setStorage(Map<Long, TraineeEntity> storage) {
        if (this.storage != null) {
            throw new IllegalStateException("Trainee storage is already initialized");
        }
        this.storage = storage;
    }

    public TraineeEntity save(TraineeEntity trainee) {
        storage.put(trainee.getId(), trainee);
        log.debug("Saved trainee id={}", trainee.getId());
        return trainee;
    }

    public void delete(Long id) {
        storage.remove(id);
        log.debug("Deleted trainee id={}", id);
    }

    public Optional<TraineeEntity> findById(Long id) {
        log.debug("findById trainee id={}", id);
        return Optional.ofNullable(storage.get(id));
    }

    public Stream<TraineeEntity> findAll() {
        log.debug("findAll trainees, count={}", storage.size());
        return storage.values().stream();
    }

    public boolean existsById(Long id) {
        return storage.containsKey(id);
    }
}
