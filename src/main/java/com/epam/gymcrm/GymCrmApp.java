package com.epam.gymcrm;

import com.epam.gymcrm.config.AppConfig;
import com.epam.gymcrm.dto.AutoScheduleTrainingRequest;
import com.epam.gymcrm.dto.CreateTraineeRequest;
import com.epam.gymcrm.dto.CreateTrainerRequest;
import com.epam.gymcrm.dto.ScheduleTrainingRequest;
import com.epam.gymcrm.dto.TraineeResponse;
import com.epam.gymcrm.dto.TrainerResponse;
import com.epam.gymcrm.dto.TrainingResponse;
import com.epam.gymcrm.dto.UserInfo;
import com.epam.gymcrm.entity.TrainingType;
import com.epam.gymcrm.service.GymService;
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
            var facade = context.getBean(GymService.class);
            var trainerService = context.getBean(TrainerService.class);
            var traineeService = context.getBean(TraineeService.class);
            var trainingService = context.getBean(TrainingService.class);

            log.info("Loaded data");
            trainerService.findAll().forEach(trainer -> log.info("Trainer: {}", trainer.userId()));
            traineeService.findAll().forEach(trainee -> log.info("Trainee: {}", trainee.userId()));
            trainingService.findAll().forEach(training ->
                    log.info("Training: {} {}", training.id(), training.name()));

            TrainerResponse newTrainer = trainerService.create(new CreateTrainerRequest(
                    new UserInfo("John", "Smith"),
                    TrainingType.PILATES));
            log.info("Created trainer id={}", newTrainer.userId());

            TraineeResponse newTrainee = traineeService.create(new CreateTraineeRequest(
                    new UserInfo("Jane", "Doe"),
                    LocalDate.of(1998, 5, 20),
                    "Kyiv"));
            log.info("Created trainee id={}", newTrainee.userId());

            TrainingResponse scheduled = facade.scheduleTraining(new ScheduleTrainingRequest(
                    newTrainee.userId(),
                    newTrainer.userId(),
                    "Evening Pilates",
                    TrainingType.PILATES,
                    LocalDate.now(),
                    Duration.ofMinutes(60)));
            log.info("Scheduled training: id={}, name={}", scheduled.id(), scheduled.name());

            TrainingResponse autoScheduled = facade.autoScheduleTraining(new AutoScheduleTrainingRequest(
                    newTrainee.userId(),
                    "Morning Yoga",
                    TrainingType.YOGA,
                    LocalDate.now().plusDays(1),
                    Duration.ofMinutes(45)));
            log.info("Auto-scheduled training: id={}, trainerId={}", autoScheduled.id(), autoScheduled.trainerId());
        }
    }
}
