package com.epum.gymcrm.dao;

import com.epum.gymcrm.model.Trainee;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Repository
public class TraineeDao {

    private Map<Long, Trainee> storage;

    @Autowired
    public void setStorage(@Qualifier("traineeStorage") Map<Long, Trainee> storage) {
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
}
