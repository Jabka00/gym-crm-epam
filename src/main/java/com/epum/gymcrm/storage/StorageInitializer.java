package com.epum.gymcrm.storage;

import com.epum.gymcrm.model.Trainee;
import com.epum.gymcrm.model.Trainer;
import com.epum.gymcrm.model.Training;
import com.epum.gymcrm.model.TrainingType;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Map;

@Slf4j
@Setter
@Component
public class StorageInitializer implements BeanPostProcessor {

    @Value("${storage.data.trainers}")
    private String trainersFile;

    @Value("${storage.data.trainees}")
    private String traineesFile;

    @Value("${storage.data.trainings}")
    private String trainingsFile;

    private Map<Long, Trainer> trainerStorage;
    private Map<Long, Trainee> traineeStorage;
    private Map<Long, Training> trainingStorage;

    @Autowired
    public void setTrainerStorage(@Qualifier("trainerStorage") Map<Long, Trainer> trainerStorage) {
        this.trainerStorage = trainerStorage;
    }

    @Autowired
    public void setTraineeStorage(@Qualifier("traineeStorage") Map<Long, Trainee> traineeStorage) {
        this.traineeStorage = traineeStorage;
    }

    @Autowired
    public void setTrainingStorage(@Qualifier("trainingStorage") Map<Long, Training> trainingStorage) {
        this.trainingStorage = trainingStorage;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if ("trainerStorage".equals(beanName)) {
            log.info("Seeding storage from CSV files");
            loadTrainers();
            loadTrainees();
            loadTrainings();
            log.info("Storage seeded: {} trainers, {} trainees, {} trainings",
                    trainerStorage.size(), traineeStorage.size(), trainingStorage.size());
        }
        return bean;
    }

    private void loadTrainers() {
        try (BufferedReader reader = openResource(trainersFile)) {
            reader.lines()
                    .skip(1)
                    .filter(line -> !line.isBlank())
                    .forEach(line -> {
                        String[] p = line.split(",");
                        Trainer t = Trainer.builder()
                                .userId(Long.parseLong(p[0].trim()))
                                .firstName(p[1].trim())
                                .lastName(p[2].trim())
                                .username(p[3].trim())
                                .password(p[4].trim())
                                .active(Boolean.parseBoolean(p[5].trim()))
                                .specialization(new TrainingType(p[6].trim()))
                                .build();
                        trainerStorage.put(t.getUserId(), t);
                        log.debug("Loaded trainer: {}", t.getUsername());
                    });
        } catch (IOException e) {
            log.error("Failed to load trainers from {}: {}", trainersFile, e.getMessage());
        }
    }

    private void loadTrainees() {
        try (BufferedReader reader = openResource(traineesFile)) {
            reader.lines()
                    .skip(1)
                    .filter(line -> !line.isBlank())
                    .forEach(line -> {
                        String[] p = line.split(",");
                        Trainee t = Trainee.builder()
                                .userId(Long.parseLong(p[0].trim()))
                                .firstName(p[1].trim())
                                .lastName(p[2].trim())
                                .username(p[3].trim())
                                .password(p[4].trim())
                                .active(Boolean.parseBoolean(p[5].trim()))
                                .dateOfBirth(LocalDate.parse(p[6].trim()))
                                .address(p[7].trim())
                                .build();
                        traineeStorage.put(t.getUserId(), t);
                        log.debug("Loaded trainee: {}", t.getUsername());
                    });
        } catch (IOException e) {
            log.error("Failed to load trainees from {}: {}", traineesFile, e.getMessage());
        }
    }

    private void loadTrainings() {
        try (BufferedReader reader = openResource(trainingsFile)) {
            reader.lines()
                    .skip(1)
                    .filter(line -> !line.isBlank())
                    .forEach(line -> {
                        String[] p = line.split(",");
                        Training t = Training.builder()
                                .trainingId(Long.parseLong(p[0].trim()))
                                .traineeId(Long.parseLong(p[1].trim()))
                                .trainerId(Long.parseLong(p[2].trim()))
                                .trainingName(p[3].trim())
                                .trainingType(new TrainingType(p[4].trim()))
                                .trainingDate(LocalDate.parse(p[5].trim()))
                                .trainingDuration(Duration.ofMinutes(Long.parseLong(p[6].trim())))
                                .build();
                        trainingStorage.put(t.getTrainingId(), t);
                        log.debug("Loaded training: {}", t.getTrainingName());
                    });
        } catch (IOException e) {
            log.error("Failed to load trainings from {}: {}", trainingsFile, e.getMessage());
        }
    }

    private BufferedReader openResource(String path) throws IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream(path);
        if (is == null) {
            throw new IOException("Classpath resource not found: " + path);
        }
        return new BufferedReader(new InputStreamReader(is));
    }
}
