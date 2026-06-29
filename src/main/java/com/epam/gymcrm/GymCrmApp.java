package com.epam.gymcrm;

import com.epam.gymcrm.config.AppConfig;
import com.epam.gymcrm.dto.request.AutoScheduleTrainingRequest;
import com.epam.gymcrm.dto.request.CreateTraineeRequest;
import com.epam.gymcrm.dto.request.CreateTrainerRequest;
import com.epam.gymcrm.dto.request.ScheduleTrainingRequest;
import com.epam.gymcrm.dto.request.UserInfo;
import com.epam.gymcrm.dto.response.Trainee;
import com.epam.gymcrm.dto.response.Trainer;
import com.epam.gymcrm.dto.response.Training;
import com.epam.gymcrm.model.TrainingType;
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

            Trainer newTrainer = trainerService.create(new CreateTrainerRequest(
                    new UserInfo("John", "Smith"),
                    TrainingType.PILATES));
            log.info("Created trainer id={}", newTrainer.userId());

            Trainee newTrainee = traineeService.create(new CreateTraineeRequest(
                    new UserInfo("Jane", "Doe"),
                    LocalDate.of(1998, 5, 20),
                    "Kyiv"));
            log.info("Created trainee id={}", newTrainee.userId());

            Training scheduled = facade.scheduleTraining(new ScheduleTrainingRequest(
                    newTrainee.userId(),
                    newTrainer.userId(),
                    "Evening Pilates",
                    TrainingType.PILATES,
                    LocalDate.now(),
                    Duration.ofMinutes(60)));
            log.info("Scheduled training: id={}, name={}", scheduled.id(), scheduled.name());

            Training autoScheduled = facade.autoScheduleTraining(new AutoScheduleTrainingRequest(
                    newTrainee.userId(),
                    "Morning Yoga",
                    TrainingType.YOGA,
                    LocalDate.now().plusDays(1),
                    Duration.ofMinutes(45)));
            log.info("Auto-scheduled training: id={}, trainerId={}", autoScheduled.id(), autoScheduled.trainerId());
        }
    }
}
