package com.epam.gymcrm.support;

import com.epam.gymcrm.dto.request.AutoScheduleTrainingRequest;
import com.epam.gymcrm.dto.request.CreateTraineeRequest;
import com.epam.gymcrm.dto.request.CreateTrainerRequest;
import com.epam.gymcrm.dto.request.ScheduleTrainingRequest;
import com.epam.gymcrm.dto.request.UpdateTraineeRequest;
import com.epam.gymcrm.dto.request.UpdateTrainerRequest;
import com.epam.gymcrm.dto.request.UserInfo;
import com.epam.gymcrm.dto.response.Trainee;
import com.epam.gymcrm.dto.response.Trainer;
import com.epam.gymcrm.dto.response.Training;
import com.epam.gymcrm.dto.response.TrainingType;
import com.epam.gymcrm.entity.TraineeEntity;
import com.epam.gymcrm.entity.TrainerEntity;
import com.epam.gymcrm.entity.TrainingEntity;
import com.epam.gymcrm.entity.TrainingTypeEntity;
import com.epam.gymcrm.security.Credentials;

import java.time.Duration;
import java.time.LocalDate;

public final class TestDataFactory {

    private TestDataFactory() {
    }

    public static TrainingTypeEntity trainingType(String typeName) {
        TrainingTypeEntity entity = new TrainingTypeEntity();
        entity.setTypeName(typeName);
        return entity;
    }

    public static TrainingTypeEntity yogaTypeEntity() {
        return trainingTypeWithId(1L, "YOGA");
    }

    public static TrainingTypeEntity crossfitTypeEntity() {
        return trainingTypeWithId(2L, "CROSSFIT");
    }

    public static TrainingTypeEntity boxingTypeEntity() {
        return trainingTypeWithId(3L, "BOXING");
    }

    public static TrainingTypeEntity pilatesTypeEntity() {
        return trainingTypeWithId(4L, "PILATES");
    }

    public static TrainingTypeEntity trainingTypeWithId(long id, String typeName) {
        TrainingTypeEntity entity = trainingType(typeName);
        entity.setId(id);
        return entity;
    }

    public static TraineeEntity seedTraineeAliceWalker() {
        TraineeEntity trainee = new TraineeEntity();
        trainee.setId(4L);
        trainee.setFirstName("Alice");
        trainee.setLastName("Walker");
        trainee.setUsername("Alice.Walker");
        trainee.setPassword("qW3eRt5yUi");
        trainee.setActive(true);
        trainee.setDateOfBirth(LocalDate.of(1995, 4, 12));
        trainee.setAddress("123 Main St");
        return trainee;
    }

    public static TrainerEntity seedTrainerJohnSmith() {
        TrainerEntity trainer = new TrainerEntity();
        trainer.setId(1L);
        trainer.setFirstName("John");
        trainer.setLastName("Smith");
        trainer.setUsername("John.Smith");
        trainer.setPassword("pass1234AB");
        trainer.setActive(true);
        trainer.setSpecialization(yogaTypeEntity());
        return trainer;
    }

    public static TrainingEntity trainingWithSeedAssociations(long trainingId) {
        TrainingEntity training = createDefaultTraining(4L, 1L);
        training.setId(trainingId);
        training.setTrainee(seedTraineeAliceWalker());
        training.setTrainer(seedTrainerJohnSmith());
        return training;
    }

    public static TrainerEntity createDefaultTrainer() {
        TrainerEntity trainer = new TrainerEntity();
        trainer.setFirstName("John");
        trainer.setLastName("Smith");
        trainer.setSpecialization(yogaTypeEntity());
        return trainer;
    }

    public static TrainerEntity createTrainerWithCredentials() {
        TrainerEntity trainer = createDefaultTrainer();
        trainer.setUsername("John.Smith");
        trainer.setPassword("secret1234");
        trainer.setActive(true);
        return trainer;
    }

    public static TrainerEntity trainerWithId(long id, String username) {
        TrainerEntity trainer = createTrainerWithCredentials();
        trainer.setId(id);
        trainer.setUsername(username);
        return trainer;
    }

    public static TrainerEntity trainer(String username) {
        TrainerEntity trainer = createTrainerWithCredentials();
        trainer.setUsername(username);
        return trainer;
    }

    public static TraineeEntity createDefaultTrainee() {
        TraineeEntity trainee = new TraineeEntity();
        trainee.setFirstName("Alice");
        trainee.setLastName("Walker");
        trainee.setDateOfBirth(LocalDate.of(1995, 4, 12));
        trainee.setAddress("Kyiv");
        return trainee;
    }

    public static TraineeEntity createTraineeWithCredentials() {
        TraineeEntity trainee = createDefaultTrainee();
        trainee.setUsername("Alice.Walker");
        trainee.setPassword("secret1234");
        trainee.setActive(true);
        return trainee;
    }

    public static TraineeEntity traineeWithId(long id, String username) {
        TraineeEntity trainee = createTraineeWithCredentials();
        trainee.setId(id);
        trainee.setUsername(username);
        return trainee;
    }

    public static TraineeEntity trainee(String username) {
        TraineeEntity trainee = createTraineeWithCredentials();
        trainee.setUsername(username);
        return trainee;
    }

    public static CreateTraineeRequest createTraineeRequest() {
        return new CreateTraineeRequest(
                new UserInfo("Jane", "Doe"),
                LocalDate.of(1998, 5, 20),
                "Kyiv");
    }

    public static CreateTraineeRequest createTraineeRequest(String firstName, String lastName) {
        return new CreateTraineeRequest(
                new UserInfo(firstName, lastName),
                LocalDate.of(1998, 5, 20),
                "Kyiv");
    }

    public static UpdateTraineeRequest updateTraineeRequest(long id) {
        return new UpdateTraineeRequest(
                id,
                new UserInfo("Alice", "Walker"),
                true,
                LocalDate.of(1995, 4, 12),
                "Kyiv");
    }

    public static Trainee traineeResponse(long id, String username) {
        return new Trainee(
                id,
                "Alice Walker",
                username,
                LocalDate.of(1995, 4, 12),
                "Kyiv");
    }

    public static Trainer trainerResponse(long id, String username) {
        return new Trainer(
                id,
                "John Smith",
                username,
                trainingTypeResponse(1L, "YOGA"));
    }

    public static CreateTrainerRequest createTrainerRequest(String specialization) {
        return new CreateTrainerRequest(new UserInfo("John", "Smith"), specialization);
    }

    public static UpdateTrainerRequest updateTrainerRequest(long id, String specialization) {
        return new UpdateTrainerRequest(id, new UserInfo("John", "Smith"), true, specialization);
    }

    public static TrainingType trainingTypeResponse(long id, String typeName) {
        return new TrainingType(id, typeName);
    }

    public static ScheduleTrainingRequest scheduleTrainingRequest(long traineeId, long trainerId, String type) {
        return scheduleTrainingRequest(traineeId, trainerId, type, LocalDate.of(2024, 3, 1));
    }

    public static ScheduleTrainingRequest scheduleTrainingRequest(
            long traineeId, long trainerId, String type, LocalDate date) {
        return new ScheduleTrainingRequest(
                traineeId,
                trainerId,
                "Morning Yoga",
                type,
                date,
                Duration.ofMinutes(60));
    }

    public static AutoScheduleTrainingRequest autoScheduleTrainingRequest(long traineeId, String type) {
        return autoScheduleTrainingRequest(traineeId, type, LocalDate.of(2024, 3, 1));
    }

    public static AutoScheduleTrainingRequest autoScheduleTrainingRequest(
            long traineeId, String type, LocalDate date) {
        return new AutoScheduleTrainingRequest(
                traineeId,
                "Morning Yoga",
                type,
                date,
                Duration.ofMinutes(60));
    }

    public static Training trainingResponse(long id) {
        return new Training(
                id,
                "Morning Yoga",
                trainingTypeResponse(1L, "YOGA"),
                LocalDate.of(2024, 3, 1),
                Duration.ofMinutes(60),
                1L,
                2L
        );
    }

    public static ScheduleTrainingRequest scheduleTrainingRequestWithoutName(long traineeId, long trainerId) {
        return new ScheduleTrainingRequest(
                traineeId,
                trainerId,
                null,
                "YOGA",
                LocalDate.of(2024, 3, 1),
                Duration.ofMinutes(60));
    }

    public static UpdateTraineeRequest updateTraineeRequestWithoutId() {
        return new UpdateTraineeRequest(
                null,
                new UserInfo("Jane", "Doe"),
                true,
                LocalDate.of(1998, 5, 20),
                "Kyiv");
    }

    public static TrainingEntity createDefaultTraining(Long traineeId, Long trainerId) {
        TrainingEntity training = new TrainingEntity();

        TraineeEntity trainee = new TraineeEntity();
        trainee.setId(traineeId);
        training.setTrainee(trainee);

        TrainerEntity trainer = new TrainerEntity();
        trainer.setId(trainerId);
        training.setTrainer(trainer);

        training.setTrainingName("Morning Yoga");
        training.setTrainingType(yogaTypeEntity());
        training.setTrainingDate(LocalDate.of(2024, 3, 1));
        training.setDurationMinutes(60);
        return training;
    }

    public static TrainingEntity trainingWithId(long id, long traineeId, long trainerId) {
        TrainingEntity training = createDefaultTraining(traineeId, trainerId);
        training.setId(id);
        return training;
    }

    public static Credentials credentials() {
        return new Credentials("Alice.Walker", "secret1234");
    }

    public static Credentials credentials(String username, String password) {
        return new Credentials(username, password);
    }

    public static Credentials aliceCredentials() {
        return new Credentials("Alice.Walker", "qW3eRt5yUi");
    }

    public static Credentials johnSmithCredentials() {
        return new Credentials("John.Smith", "pass1234AB");
    }
}
