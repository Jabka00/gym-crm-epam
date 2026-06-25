package com.epam.gymcrm.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.IOException;
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
        if (bean instanceof TrainerStorage storage) {
            seedIfEmpty(storage, () -> StorageCsvSeeder.seedTrainers(
                    storage, environment.getRequiredProperty("storage.data.trainers")));
        } else if (bean instanceof TraineeStorage storage) {
            seedIfEmpty(storage, () -> StorageCsvSeeder.seedTrainees(
                    storage, environment.getRequiredProperty("storage.data.trainees")));
        } else if (bean instanceof TrainingStorage storage) {
            seedIfEmpty(storage, () -> StorageCsvSeeder.seedTrainings(
                    storage, environment.getRequiredProperty("storage.data.trainings")));
        }
        return bean;
    }

    private void seedIfEmpty(Map<?, ?> storage, IoRunnable seeder) {
        if (!storage.isEmpty()) {
            return;
        }
        try {
            seeder.run();
            log.info("Seeded {} entries into {}", storage.size(), storage.getClass().getSimpleName());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to seed " + storage.getClass().getSimpleName(), e);
        }
    }

    @FunctionalInterface
    private interface IoRunnable {
        void run() throws IOException;
    }
}
