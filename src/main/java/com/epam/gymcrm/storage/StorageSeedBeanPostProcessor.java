package com.epam.gymcrm.storage;

import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.Training;
import com.epam.gymcrm.repository.TraineeRepository;
import com.epam.gymcrm.repository.TrainerRepository;
import com.epam.gymcrm.repository.TrainingRepository;
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
            if (bean instanceof TrainerRepository repository) {
                Map<Long, Trainer> storage = new HashMap<>();
                for (Trainer trainer : StorageCsvSeeder.readTrainers(
                        environment.getRequiredProperty("storage.data.trainers"))) {
                    storage.put(trainer.getUserId(), trainer);
                }
                repository.setStorage(storage);
                log.info("Initialized trainer storage with {} entries", storage.size());
            } else if (bean instanceof TraineeRepository repository) {
                Map<Long, Trainee> storage = new HashMap<>();
                for (Trainee trainee : StorageCsvSeeder.readTrainees(
                        environment.getRequiredProperty("storage.data.trainees"))) {
                    storage.put(trainee.getUserId(), trainee);
                }
                repository.setStorage(storage);
                log.info("Initialized trainee storage with {} entries", storage.size());
            } else if (bean instanceof TrainingRepository repository) {
                Map<Long, Training> storage = new HashMap<>();
                for (Training training : StorageCsvSeeder.readTrainings(
                        environment.getRequiredProperty("storage.data.trainings"))) {
                    storage.put(training.getTrainingId(), training);
                }
                repository.setStorage(storage);
                log.info("Initialized training storage with {} entries", storage.size());
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to initialize repository bean: " + beanName, e);
        }
        return bean;
    }
}
