package com.epam.gymcrm;

import com.epam.gymcrm.config.AppConfig;
import com.epam.gymcrm.dto.TraineeDto;
import com.epam.gymcrm.dto.TrainerDto;
import com.epam.gymcrm.dto.TrainingDto;
import com.epam.gymcrm.dto.TrainingTypeDto;
import com.epam.gymcrm.service.GymService;
import com.epam.gymcrm.service.TraineeService;
import com.epam.gymcrm.service.TrainerService;
import com.epam.gymcrm.service.TrainingService;
import com.epam.gymcrm.service.TrainingTypeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDate;

@Slf4j
public class GymCrmApp {

    public static void main(String[] args) {
        log.info("Starting Gym CRM Application...");

        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class)) {
            ApplicationContext applicationContext = context;

            GymService gymService = applicationContext.getBean(GymService.class);
            TraineeService traineeService = applicationContext.getBean(TraineeService.class);
            TrainerService trainerService = applicationContext.getBean(TrainerService.class);
            TrainingService trainingService = applicationContext.getBean(TrainingService.class);
            TrainingTypeService trainingTypeService = applicationContext.getBean(TrainingTypeService.class);

            log.info("Loaded trainers: {}", trainerService.getAllTrainers().size());
            log.info("Loaded trainees: {}", traineeService.getAllTrainees().size());
            log.info("Loaded trainings: {}", trainingService.getAllTrainings().size());

            TraineeDto traineeDto = TraineeDto.builder()
                    .firstName("Jane")
                    .lastName("Doe")
                    .dateOfBirth(LocalDate.of(1998, 5, 20))
                    .address("Kyiv")
                    .build();

            TraineeDto createdTrainee = traineeService.createTrainee(traineeDto);
            log.info("Created trainee id={}, username={}", createdTrainee.getId(), createdTrainee.getUsername());

            TrainingTypeDto pilatesType = trainingTypeService.getTrainingTypeByName("PILATES");

            TrainerDto trainerDto = TrainerDto.builder()
                    .firstName("John")
                    .lastName("Smith")
                    .specialization(pilatesType)
                    .build();

            TrainerDto createdTrainer = trainerService.createTrainer(trainerDto);
            log.info("Created trainer id={}, username={}", createdTrainer.getId(), createdTrainer.getUsername());

            TrainingDto newTraining = TrainingDto.builder()
                    .trainee(createdTrainee)
                    .trainer(createdTrainer)
                    .trainingName("Evening Pilates")
                    .trainingType(pilatesType)
                    .trainingDate(LocalDate.now())
                    .trainingDuration(60)
                    .build();

            TrainingDto createdTraining = trainingService.createTraining(newTraining);
            log.info("Created training id={}, name={}", createdTraining.getId(), createdTraining.getTrainingName());

            TraineeDto fetchedTrainee = traineeService.getTraineeByUsername(createdTrainee.getUsername());
            TrainerDto fetchedTrainer = trainerService.getTrainerByUsername(createdTrainer.getUsername());
            TrainingDto fetchedTraining = trainingService.getTraining(createdTraining.getId());

            log.info("Fetched trainee: {}", fetchedTrainee.getUsername());
            log.info("Fetched trainer: {}", fetchedTrainer.getUsername());
            log.info("Fetched training: {}", fetchedTraining.getTrainingName());

            gymService.removeTraineeProfile(createdTrainee.getId());
            log.info("Demo completed successfully");
        } catch (Exception e) {
            log.error("Error running application", e);
        }
    }
}
