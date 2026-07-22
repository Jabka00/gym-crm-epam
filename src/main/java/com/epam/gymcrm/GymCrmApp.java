package com.epam.gymcrm;

import com.epam.gymcrm.config.AppConfig;
import com.epam.gymcrm.dto.Credentials;
import com.epam.gymcrm.dto.Trainee;
import com.epam.gymcrm.dto.Trainer;
import com.epam.gymcrm.dto.Training;
import com.epam.gymcrm.dto.request.ChangePasswordRequest;
import com.epam.gymcrm.dto.request.CreateTraineeRequest;
import com.epam.gymcrm.dto.request.CreateTrainerRequest;
import com.epam.gymcrm.dto.request.ScheduleTrainingRequest;
import com.epam.gymcrm.dto.request.ToggleActivationRequest;
import com.epam.gymcrm.dto.request.UserInfo;
import com.epam.gymcrm.exception.InvalidOperationException;
import com.epam.gymcrm.model.TrainingType;
import com.epam.gymcrm.repository.TraineeRepository;
import com.epam.gymcrm.repository.TrainerRepository;
import com.epam.gymcrm.service.AuthenticationService;
import com.epam.gymcrm.service.TraineeService;
import com.epam.gymcrm.service.TrainerService;
import com.epam.gymcrm.service.TrainingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Slf4j
public class GymCrmApp {

    public static void main(String[] args) {
        log.info("Starting Gym CRM Application...");

        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class)) {
            ApplicationContext applicationContext = context;

            TraineeService traineeService = applicationContext.getBean(TraineeService.class);
            TrainerService trainerService = applicationContext.getBean(TrainerService.class);
            TrainingService trainingService = applicationContext.getBean(TrainingService.class);
            AuthenticationService authenticationService = applicationContext.getBean(AuthenticationService.class);
            TraineeRepository traineeRepository = applicationContext.getBean(TraineeRepository.class);
            TrainerRepository trainerRepository = applicationContext.getBean(TrainerRepository.class);

            CreateTraineeRequest disposableRequest = new CreateTraineeRequest(
                    new UserInfo("Jane", "Doe"),
                    LocalDate.of(1998, 5, 20),
                    "Kyiv"
            );
            Trainee disposableTrainee = traineeService.createTrainee(disposableRequest);
            Credentials disposableAuth = credentialsOf(disposableTrainee, traineeRepository);
            traineeService.deleteTraineeByUsername(disposableAuth, disposableTrainee.username());
            log.info("Disposable trainee created and deleted");

            CreateTrainerRequest pilatesTrainerRequest = new CreateTrainerRequest(
                    new UserInfo("Emma", "Pilates"),
                    TrainingType.PILATES
            );
            Trainer pilatesTrainer = trainerService.createTrainer(pilatesTrainerRequest);
            Credentials pilatesAuth = credentialsOf(pilatesTrainer, trainerRepository);
            log.info("Trainer created");
            logTrainerAuthentication(authenticationService, pilatesAuth);

            CreateTraineeRequest kateRequest = new CreateTraineeRequest(
                    new UserInfo("Kate", "Doe"),
                    LocalDate.of(1999, 3, 10),
                    "Lviv"
            );
            Trainee kate = traineeService.createTrainee(kateRequest);
            Credentials kateAuth = credentialsOf(kate, traineeRepository);
            log.info("Trainee created");
            logTraineeAuthentication(authenticationService, kateAuth);

            ScheduleTrainingRequest scheduleRequest = new ScheduleTrainingRequest(
                    kate.userId(),
                    pilatesTrainer.userId(),
                    "Evening Pilates",
                    TrainingType.PILATES,
                    LocalDate.now(),
                    Duration.ofMinutes(60)
            );
            Training scheduledTraining = trainingService.createTraining(kateAuth, scheduleRequest);
            log.info("Training created, id present={}", scheduledTraining.id() != null);

            List<Trainer> unassignedTrainers = traineeService.getNotAssignedTrainers(kateAuth, kate.username());
            log.info("Unassigned trainers count={}", unassignedTrainers.size());

            traineeService.updateTrainersList(kateAuth, kate.username(), Set.of("John.Smith", "Anna.Jones"));
            log.info("Trainers list updated");

            List<Trainer> unassignedAfterUpdate = traineeService.getNotAssignedTrainers(kateAuth, kate.username());
            log.info("Unassigned trainers after update count={}", unassignedAfterUpdate.size());

            Credentials aliceAuth = new Credentials("Alice.Walker", "qW3eRt5yUi");
            Credentials johnAuth = new Credentials("John.Smith", "pass1234AB");

            List<Training> aliceTrainings = trainingService.getTraineeTrainings(
                    aliceAuth,
                    "Alice.Walker",
                    LocalDate.of(2024, 3, 1),
                    LocalDate.of(2024, 3, 31),
                    "John.Smith",
                    TrainingType.YOGA);
            log.info("Trainee filtered trainings count={}", aliceTrainings.size());

            List<Training> johnTrainings = trainingService.getTrainerTrainings(
                    johnAuth,
                    "John.Smith",
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 12, 31),
                    null);
            log.info("Trainer filtered trainings count={}", johnTrainings.size());

            String newPassword = "SecurePass1";
            traineeService.changePassword(
                    kateAuth,
                    new ChangePasswordRequest(kate.username(), kateAuth.password(), newPassword));
            kateAuth = new Credentials(kate.username(), newPassword);
            log.info("Trainee password changed");
            logTraineeAuthentication(authenticationService, kateAuth);

            traineeService.toggleActivation(kateAuth, new ToggleActivationRequest(kate.username()));
            log.info("Trainee deactivated");

            try {
                traineeService.getTraineeByUsername(aliceAuth, kate.username());
                log.error("Expected inactive trainee lookup to fail");
            } catch (InvalidOperationException e) {
                log.info("Inactive trainee correctly hidden");
            }

            logTraineeAuthentication(authenticationService, kateAuth);

            Trainer fetchedTrainer = trainerService.getTrainerByUsername(pilatesAuth, pilatesTrainer.username());
            log.info("Trainer fetched, username present={}", fetchedTrainer.username() != null);

            log.info("Demo completed successfully");
        } catch (Exception e) {
            log.error("Error running application", e);
        }
    }

    private static Credentials credentialsOf(Trainee trainee, TraineeRepository traineeRepository) {
        String password = traineeRepository.findByUsername(trainee.username())
                .orElseThrow(() -> new IllegalStateException("Trainee not found"))
                .getUser()
                .getPassword();
        return new Credentials(trainee.username(), password);
    }

    private static Credentials credentialsOf(Trainer trainer, TrainerRepository trainerRepository) {
        String password = trainerRepository.findByUsername(trainer.username())
                .orElseThrow(() -> new IllegalStateException("Trainer not found"))
                .getUser()
                .getPassword();
        return new Credentials(trainer.username(), password);
    }

    private static void logTraineeAuthentication(AuthenticationService authenticationService, Credentials credentials) {
        boolean matched = authenticationService.matchesTraineeCredentials(
                credentials.username(), credentials.password());
        log.info("Trainee authentication: {}", matched ? "success" : "failed");
    }

    private static void logTrainerAuthentication(AuthenticationService authenticationService, Credentials credentials) {
        boolean matched = authenticationService.matchesTrainerCredentials(
                credentials.username(), credentials.password());
        log.info("Trainer authentication: {}", matched ? "success" : "failed");
    }
}
