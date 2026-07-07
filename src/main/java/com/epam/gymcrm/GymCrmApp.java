package com.epam.gymcrm;

import com.epam.gymcrm.config.AppConfig;
import com.epam.gymcrm.dto.request.AutoScheduleTrainingRequest;
import com.epam.gymcrm.dto.request.CreateTraineeRequest;
import com.epam.gymcrm.dto.request.CreateTrainerRequest;
import com.epam.gymcrm.dto.request.ScheduleTrainingRequest;
import com.epam.gymcrm.dto.response.Trainee;
import com.epam.gymcrm.dto.response.Trainer;
import com.epam.gymcrm.dto.response.Training;
import com.epam.gymcrm.exception.InvalidOperationException;
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

            log.info("Loaded trainers: {}", trainerService.getAllTrainers().size());
            log.info("Loaded trainees: {}", traineeService.getAllTrainees().size());
            log.info("Loaded trainings: {}", trainingService.getAllTrainings().size());

            CreateTraineeRequest disposableRequest = new CreateTraineeRequest();
            disposableRequest.setFirstName("Jane");
            disposableRequest.setLastName("Doe");
            disposableRequest.setDateOfBirth(LocalDate.of(1998, 5, 20));
            disposableRequest.setAddress("Kyiv");
            Trainee disposableTrainee = traineeService.createTrainee(disposableRequest);
            log.info("Created trainee id={}, username={}", disposableTrainee.getId(), disposableTrainee.getUsername());

            Credentials disposableAuth = credentialsOf(disposableTrainee);
            gymService.removeTraineeProfile(disposableAuth, disposableTrainee.getUsername());
            log.info("Removed trainee username={}", disposableTrainee.getUsername());

            trainingTypeService.getTrainingTypeByName("PILATES");
            trainingTypeService.getTrainingTypeByName("YOGA");

            CreateTrainerRequest pilatesTrainerRequest = new CreateTrainerRequest();
            pilatesTrainerRequest.setFirstName("Emma");
            pilatesTrainerRequest.setLastName("Pilates");
            pilatesTrainerRequest.setSpecialization("PILATES");
            Trainer pilatesTrainer = trainerService.createTrainer(pilatesTrainerRequest);
            Credentials pilatesAuth = credentialsOf(pilatesTrainer);
            log.info("Created trainer id={}, username={}", pilatesTrainer.getId(), pilatesTrainer.getUsername());
            logTrainerAuthentication(authenticationService, pilatesAuth);

            CreateTraineeRequest kateRequest = new CreateTraineeRequest();
            kateRequest.setFirstName("Kate");
            kateRequest.setLastName("Doe");
            kateRequest.setDateOfBirth(LocalDate.of(1999, 3, 10));
            kateRequest.setAddress("Lviv");
            Trainee kate = traineeService.createTrainee(kateRequest);
            Credentials kateAuth = credentialsOf(kate);
            log.info("Created trainee id={}, username={}", kate.getId(), kate.getUsername());
            logTraineeAuthentication(authenticationService, kateAuth);

            ScheduleTrainingRequest scheduleRequest = new ScheduleTrainingRequest();
            scheduleRequest.setTraineeId(kate.getId());
            scheduleRequest.setTrainerId(pilatesTrainer.getId());
            scheduleRequest.setTrainingName("Evening Pilates");
            scheduleRequest.setTrainingType("PILATES");
            scheduleRequest.setTrainingDate(LocalDate.now());
            scheduleRequest.setTrainingDuration(60);
            Training scheduledTraining = gymService.scheduleTraining(kateAuth, scheduleRequest);
            log.info("Scheduled training id={}, name={}", scheduledTraining.getId(), scheduledTraining.getTrainingName());

            AutoScheduleTrainingRequest autoScheduleRequest = new AutoScheduleTrainingRequest();
            autoScheduleRequest.setTraineeId(kate.getId());
            autoScheduleRequest.setTrainingName("Morning Yoga");
            autoScheduleRequest.setTrainingType("YOGA");
            autoScheduleRequest.setTrainingDate(LocalDate.now().plusDays(1));
            autoScheduleRequest.setTrainingDuration(45);
            Training autoScheduledTraining = gymService.autoScheduleTraining(kateAuth, autoScheduleRequest);
            log.info("Auto-scheduled training id={}, trainer={}",
                    autoScheduledTraining.getId(), autoScheduledTraining.getTrainer().getUsername());

            List<Trainer> unassignedTrainers = traineeService.getNotAssignedTrainers(kateAuth, kate.getUsername());
            log.info("Unassigned active trainers for {}: {}", kate.getUsername(), unassignedTrainers.size());

            traineeService.updateTrainersList(kateAuth, kate.getUsername(), Set.of("John.Smith", "Anna.Jones"));
            log.info("Assigned seed trainers to {}", kate.getUsername());

            List<Trainer> unassignedAfterUpdate = traineeService.getNotAssignedTrainers(kateAuth, kate.getUsername());
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
            traineeService.changePassword(kateAuth, kate.getUsername(), kateAuth.password(), newPassword);
            kateAuth = new Credentials(kate.getUsername(), newPassword);
            log.info("Password changed for {}", kate.getUsername());
            logTraineeAuthentication(authenticationService, kateAuth);

            traineeService.toggleActivation(kateAuth, kate.getUsername());
            log.info("Deactivated trainee {}", kate.getUsername());

            try {
                traineeService.getTraineeByUsername(aliceAuth, kate.getUsername());
                log.error("Expected inactive trainee lookup to fail");
            } catch (InvalidOperationException e) {
                log.info("Inactive trainee correctly hidden: {}", e.getMessage());
            }

            logTraineeAuthentication(authenticationService, kateAuth);

            Training fetchedScheduled = trainingService.getTraining(scheduledTraining.getId());
            Trainer fetchedTrainer = trainerService.getTrainerByUsername(pilatesAuth, pilatesTrainer.getUsername());
            log.info("Fetched training: {}", fetchedScheduled.getTrainingName());
            log.info("Fetched trainer: {}", fetchedTrainer.getUsername());

            log.info("Demo completed successfully");
        } catch (Exception e) {
            log.error("Error running application", e);
        }
    }

    private static Credentials credentialsOf(Trainee trainee) {
        return new Credentials(trainee.getUsername(), trainee.getPassword());
    }

    private static Credentials credentialsOf(Trainer trainer) {
        return new Credentials(trainer.getUsername(), trainer.getPassword());
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
