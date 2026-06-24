package com.epum.gymcrm.dao;

import com.epum.gymcrm.model.Training;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Repository
public class TrainingDao {

    private Map<Long, Training> storage;

    @Autowired
    public void setStorage(@Qualifier("trainingStorage") Map<Long, Training> storage) {
        this.storage = storage;
    }

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
}
