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
import com.epam.gymcrm.exception.InvalidOperationException;
import com.epam.gymcrm.repository.TraineeRepository;
import com.epam.gymcrm.repository.TrainerRepository;
import com.epam.gymcrm.security.Credentials;
import com.epam.gymcrm.service.AuthenticationService;
import com.epam.gymcrm.service.GymService;
import com.epam.gymcrm.service.TraineeService;
import com.epam.gymcrm.service.TrainerService;
import com.epam.gymcrm.service.TrainingService;
import com.epam.gymcrm.service.TrainingTypeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

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
            AuthenticationService authenticationService = applicationContext.getBean(AuthenticationService.class);
            TraineeRepository traineeRepository = applicationContext.getBean(TraineeRepository.class);
            TrainerRepository trainerRepository = applicationContext.getBean(TrainerRepository.class);

            log.info("Loaded trainers: {}", trainerService.getAllTrainers().size());
            log.info("Loaded trainees: {}", traineeService.getAllTrainees().size());
            log.info("Loaded trainings: {}", trainingService.getAllTrainings().size());

            CreateTraineeRequest disposableRequest = new CreateTraineeRequest(
                    new UserInfo("Jane", "Doe"),
                    LocalDate.of(1998, 5, 20),
                    "Kyiv"
            );
            Trainee disposableTrainee = traineeService.createTrainee(disposableRequest);
            log.info("Created trainee id={}, username={}", disposableTrainee.userId(), disposableTrainee.username());

            Credentials disposableAuth = credentialsOf(disposableTrainee, traineeRepository);
            gymService.removeTraineeProfile(disposableAuth, disposableTrainee.username());
            log.info("Removed trainee username={}", disposableTrainee.username());

            trainingTypeService.getTrainingTypeByName("PILATES");
            trainingTypeService.getTrainingTypeByName("YOGA");

            CreateTrainerRequest pilatesTrainerRequest = new CreateTrainerRequest(
                    new UserInfo("Emma", "Pilates"),
                    "PILATES"
            );
            Trainer pilatesTrainer = trainerService.createTrainer(pilatesTrainerRequest);
            Credentials pilatesAuth = credentialsOf(pilatesTrainer, trainerRepository);
            log.info("Created trainer id={}, username={}", pilatesTrainer.userId(), pilatesTrainer.username());
            logTrainerAuthentication(authenticationService, pilatesAuth);

            CreateTraineeRequest kateRequest = new CreateTraineeRequest(
                    new UserInfo("Kate", "Doe"),
                    LocalDate.of(1999, 3, 10),
                    "Lviv"
            );
            Trainee kate = traineeService.createTrainee(kateRequest);
            Credentials kateAuth = credentialsOf(kate, traineeRepository);
            log.info("Created trainee id={}, username={}", kate.userId(), kate.username());
            logTraineeAuthentication(authenticationService, kateAuth);

            ScheduleTrainingRequest scheduleRequest = new ScheduleTrainingRequest(
                    kate.userId(),
                    pilatesTrainer.userId(),
                    "Evening Pilates",
                    "PILATES",
                    LocalDate.now(),
                    60
            );
            Training scheduledTraining = gymService.scheduleTraining(kateAuth, scheduleRequest);
            log.info("Scheduled training id={}, name={}", scheduledTraining.id(), scheduledTraining.name());

            AutoScheduleTrainingRequest autoScheduleRequest = new AutoScheduleTrainingRequest(
                    kate.userId(),
                    "Morning Yoga",
                    "YOGA",
                    LocalDate.now().plusDays(1),
                    45
            );
            Training autoScheduledTraining = gymService.autoScheduleTraining(kateAuth, autoScheduleRequest);
            log.info("Auto-scheduled training id={}, trainerId={}",
                    autoScheduledTraining.id(), autoScheduledTraining.trainerId());

            List<Trainer> unassignedTrainers = traineeService.getNotAssignedTrainers(kateAuth, kate.username());
            log.info("Unassigned active trainers for {}: {}", kate.username(), unassignedTrainers.size());

            traineeService.updateTrainersList(kateAuth, kate.username(), Set.of("John.Smith", "Anna.Jones"));
            log.info("Assigned seed trainers to {}", kate.username());

            List<Trainer> unassignedAfterUpdate = traineeService.getNotAssignedTrainers(kateAuth, kate.username());
            log.info("Unassigned trainers after update: {}", unassignedAfterUpdate.size());

            Credentials aliceAuth = new Credentials("Alice.Walker", "qW3eRt5yUi");
            Credentials johnAuth = new Credentials("John.Smith", "pass1234AB");

            List<Training> aliceTrainings = trainingService.getTraineeTrainings(
                    aliceAuth,
                    "Alice.Walker",
                    LocalDate.of(2024, 3, 1),
                    LocalDate.of(2024, 3, 31),
                    "John.Smith",
                    "YOGA");
            log.info("Alice.Walker YOGA trainings with John.Smith in March 2024: {}", aliceTrainings.size());

            List<Training> johnTrainings = trainingService.getTrainerTrainings(
                    johnAuth,
                    "John.Smith",
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 12, 31),
                    null);
            log.info("John.Smith trainings in 2024: {}", johnTrainings.size());

            String newPassword = "SecurePass1";
            traineeService.changePassword(kateAuth, kate.username(), kateAuth.password(), newPassword);
            kateAuth = new Credentials(kate.username(), newPassword);
            log.info("Password changed for {}", kate.username());
            logTraineeAuthentication(authenticationService, kateAuth);

            traineeService.toggleActivation(kateAuth, kate.username());
            log.info("Deactivated trainee {}", kate.username());

            try {
                traineeService.getTraineeByUsername(aliceAuth, kate.username());
                log.error("Expected inactive trainee lookup to fail");
            } catch (InvalidOperationException e) {
                log.info("Inactive trainee correctly hidden: {}", e.getMessage());
            }

            logTraineeAuthentication(authenticationService, kateAuth);

            Training fetchedScheduled = trainingService.getTraining(scheduledTraining.id());
            Trainer fetchedTrainer = trainerService.getTrainerByUsername(pilatesAuth, pilatesTrainer.username());
            log.info("Fetched training: {}", fetchedScheduled.name());
            log.info("Fetched trainer: {}", fetchedTrainer.username());

            log.info("Demo completed successfully");
        } catch (Exception e) {
            log.error("Error running application", e);
        }
    }

    private static Credentials credentialsOf(Trainee trainee, TraineeRepository traineeRepository) {
        String password = traineeRepository.findByUsername(trainee.username())
                .orElseThrow(() -> new IllegalStateException("Trainee not found: " + trainee.username()))
                .getPassword();
        return new Credentials(trainee.username(), password);
    }

    private static Credentials credentialsOf(Trainer trainer, TrainerRepository trainerRepository) {
        String password = trainerRepository.findByUsername(trainer.username())
                .orElseThrow(() -> new IllegalStateException("Trainer not found: " + trainer.username()))
                .getPassword();
        return new Credentials(trainer.username(), password);
    }

    private static void logTraineeAuthentication(AuthenticationService authenticationService, Credentials credentials) {
        boolean authenticated = authenticationService.authenticateTrainee(
                credentials.username(), credentials.password());
        log.info("Trainee authentication for {}: {}", credentials.username(), authenticated ? "success" : "failed");
    }

    private static void logTrainerAuthentication(AuthenticationService authenticationService, Credentials credentials) {
        boolean authenticated = authenticationService.authenticateTrainer(
                credentials.username(), credentials.password());
        log.info("Trainer authentication for {}: {}", credentials.username(), authenticated ? "success" : "failed");
    }
}
