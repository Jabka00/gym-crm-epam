package com.epam.gymcrm.config;

import com.epam.gymcrm.entity.TraineeEntity;
import com.epam.gymcrm.entity.TrainerEntity;
import com.epam.gymcrm.entity.TrainingEntity;
import com.epam.gymcrm.repository.TraineeRepository;
import com.epam.gymcrm.repository.TrainerRepository;
import com.epam.gymcrm.repository.TrainingRepository;
import com.epam.gymcrm.util.CsvDataReader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class StorageSeedBeanPostProcessor implements BeanPostProcessor {

    private String trainersResource;
    private String traineesResource;
    private String trainingsResource;

    @Value("${storage.data.trainers}")
    public void setTrainersResource(String trainersResource) {
        this.trainersResource = trainersResource;
    }

    @Value("${storage.data.trainees}")
    public void setTraineesResource(String traineesResource) {
        this.traineesResource = traineesResource;
    }

    @Value("${storage.data.trainings}")
    public void setTrainingsResource(String trainingsResource) {
        this.trainingsResource = trainingsResource;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        try {
            switch (bean) {
                case TrainerRepository repository -> {
                    Map<Long, TrainerEntity> storage = new HashMap<>();
                    for (TrainerEntity trainer : CsvDataReader.readTrainers(trainersResource)) {
                        storage.put(trainer.getId(), trainer);
                    }
                    repository.setStorage(storage);
                    log.info("Initialized trainer storage with {} entries", storage.size());
                }
                case TraineeRepository repository -> {
                    Map<Long, TraineeEntity> storage = new HashMap<>();
                    for (TraineeEntity trainee : CsvDataReader.readTrainees(traineesResource)) {
                        storage.put(trainee.getId(), trainee);
                    }
                    repository.setStorage(storage);
                    log.info("Initialized trainee storage with {} entries", storage.size());
                }
                case TrainingRepository repository -> {
                    Map<Long, TrainingEntity> storage = new HashMap<>();
                    for (TrainingEntity training : CsvDataReader.readTrainings(trainingsResource)) {
                        storage.put(training.getId(), training);
                    }
                    repository.setStorage(storage);
                    log.info("Initialized training storage with {} entries", storage.size());
                }
                default -> { }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to initialize repository bean: " + beanName, e);
        }
        return bean;
    }
}
