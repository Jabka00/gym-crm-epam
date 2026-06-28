package com.epam.gymcrm.config;

import com.epam.gymcrm.entity.TraineeEntity;
import com.epam.gymcrm.entity.TrainerEntity;
import com.epam.gymcrm.entity.TrainingEntity;
import com.epam.gymcrm.repository.TraineeRepository;
import com.epam.gymcrm.repository.TrainerRepository;
import com.epam.gymcrm.repository.TrainingRepository;
import com.epam.gymcrm.storage.StorageCsvSeeder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class StorageSeedBeanPostProcessor implements BeanPostProcessor, EnvironmentAware {

    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        try {
            switch (bean) {
                case TrainerRepository repository -> {
                    Map<Long, TrainerEntity> storage = new HashMap<>();
                    for (TrainerEntity trainer : StorageCsvSeeder.readTrainers(
                            environment.getRequiredProperty("storage.data.trainers"))) {
                        storage.put(trainer.getUserId(), trainer);
                    }
                    repository.setStorage(storage);
                    log.info("Initialized trainer storage with {} entries", storage.size());
                }
                case TraineeRepository repository -> {
                    Map<Long, TraineeEntity> storage = new HashMap<>();
                    for (TraineeEntity trainee : StorageCsvSeeder.readTrainees(
                            environment.getRequiredProperty("storage.data.trainees"))) {
                        storage.put(trainee.getUserId(), trainee);
                    }
                    repository.setStorage(storage);
                    log.info("Initialized trainee storage with {} entries", storage.size());
                }
                case TrainingRepository repository -> {
                    Map<Long, TrainingEntity> storage = new HashMap<>();
                    for (TrainingEntity training : StorageCsvSeeder.readTrainings(
                            environment.getRequiredProperty("storage.data.trainings"))) {
                        storage.put(training.getTrainingId(), training);
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
