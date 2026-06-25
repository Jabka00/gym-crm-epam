package com.epam.gymcrm.repository;

import com.epam.gymcrm.model.Trainee;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Repository
public class TraineeRepository {

    private final Map<Long, Trainee> storage = new HashMap<>();

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

    public void load(Collection<Trainee> trainees) {
        trainees.forEach(trainee -> storage.put(trainee.getUserId(), trainee));
        log.debug("Loaded {} trainees into storage", trainees.size());
    }
}
