package com.epam.gymcrm.storage;

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
            if (bean instanceof TrainerRepository repository && repository.findAll().isEmpty()) {
                var trainers = StorageCsvSeeder.readTrainers(
                        environment.getRequiredProperty("storage.data.trainers"));
                repository.load(trainers);
                log.info("Seeded {} trainers", trainers.size());
            } else if (bean instanceof TraineeRepository repository && repository.findAll().isEmpty()) {
                var trainees = StorageCsvSeeder.readTrainees(
                        environment.getRequiredProperty("storage.data.trainees"));
                repository.load(trainees);
                log.info("Seeded {} trainees", trainees.size());
            } else if (bean instanceof TrainingRepository repository && repository.findAll().isEmpty()) {
                var trainings = StorageCsvSeeder.readTrainings(
                        environment.getRequiredProperty("storage.data.trainings"));
                repository.load(trainings);
                log.info("Seeded {} trainings", trainings.size());
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to seed repository bean: " + beanName, e);
        }
        return bean;
    }
}
