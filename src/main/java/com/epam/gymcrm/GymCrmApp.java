package com.epam.gymcrm;

import com.epam.gymcrm.config.AppConfig;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.Training;
import com.epam.gymcrm.model.TrainingType;
import com.epam.gymcrm.service.GymFacade;
import com.epam.gymcrm.service.TraineeService;
import com.epam.gymcrm.service.TrainerService;
import com.epam.gymcrm.service.TrainingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.Duration;
import java.time.LocalDate;

@Slf4j
public class GymCrmApp {

    public static void main(String[] args) {
        try (var context = new AnnotationConfigApplicationContext(AppConfig.class)) {
            var facade = context.getBean(GymFacade.class);
            var trainerService = context.getBean(TrainerService.class);
            var traineeService = context.getBean(TraineeService.class);
            var trainingService = context.getBean(TrainingService.class);

            log.info("Loaded data");
            trainerService.findAll().forEach(trainer ->
                    log.info("Trainer: {} {}", trainer.getUserId(), trainer.getUsername()));
            traineeService.findAll().forEach(trainee ->
                    log.info("Trainee: {} {}", trainee.getUserId(), trainee.getUsername()));
            trainingService.findAll().forEach(training ->
                    log.info("Training: {} {}", training.getTrainingId(), training.getTrainingName()));

            Trainer newTrainer = new Trainer();
            newTrainer.setFirstName("John");
            newTrainer.setLastName("Smith");
            newTrainer.setSpecialization(new TrainingType("Pilates"));
            newTrainer = trainerService.create(newTrainer);
            log.info("Created trainer: username={}, password={}",
                    newTrainer.getUsername(), newTrainer.getPassword());

            Trainee newTrainee = new Trainee();
            newTrainee.setFirstName("Jane");
            newTrainee.setLastName("Doe");
            newTrainee.setDateOfBirth(LocalDate.of(1998, 5, 20));
            newTrainee.setAddress("Kyiv");
            newTrainee = traineeService.create(newTrainee);
            log.info("Created trainee: username={}, password={}",
                    newTrainee.getUsername(), newTrainee.getPassword());

            Training trainingToSchedule = new Training();
            trainingToSchedule.setTraineeId(newTrainee.getUserId());
            trainingToSchedule.setTrainerId(newTrainer.getUserId());
            trainingToSchedule.setTrainingName("Evening Pilates");
            trainingToSchedule.setTrainingType(new TrainingType("Pilates"));
            trainingToSchedule.setTrainingDate(LocalDate.now());
            trainingToSchedule.setTrainingDuration(Duration.ofMinutes(60));
            Training scheduled = facade.scheduleTraining(trainingToSchedule);
            log.info("Scheduled training: id={}, name={}",
                    scheduled.getTrainingId(), scheduled.getTrainingName());

            Training autoScheduled = facade.scheduleTrainingWithAvailableTrainer(
                    newTrainee.getUserId(),
                    "Morning Yoga",
                    new TrainingType("Yoga"),
                    LocalDate.now().plusDays(1),
                    Duration.ofMinutes(45));
            log.info("Auto-scheduled training: id={}, trainerId={}",
                    autoScheduled.getTrainingId(), autoScheduled.getTrainerId());
        }
    }
}
