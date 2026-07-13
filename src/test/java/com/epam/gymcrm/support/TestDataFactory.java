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
import com.epam.gymcrm.dto.response.TrainingTypeResponse;
import com.epam.gymcrm.entity.TraineeEntity;
import com.epam.gymcrm.entity.TrainerEntity;
import com.epam.gymcrm.entity.TrainingEntity;
import com.epam.gymcrm.entity.TrainingTypeEntity;
import com.epam.gymcrm.entity.UserEntity;
import com.epam.gymcrm.model.TrainingType;
import com.epam.gymcrm.dto.Credentials;

import java.time.Duration;
import java.time.LocalDate;

public final class TestDataFactory {

    private TestDataFactory() {
    }

    public static TrainingTypeEntity trainingType(TrainingType typeName) {
        TrainingTypeEntity entity = new TrainingTypeEntity();
        entity.setTypeName(typeName);
        return entity;
    }

    public static TrainingTypeEntity yogaTypeEntity() {
        return trainingTypeWithId(1L, TrainingType.YOGA);
    }

    public static TrainingTypeEntity crossfitTypeEntity() {
        return trainingTypeWithId(2L, TrainingType.CROSSFIT);
    }

    public static TrainingTypeEntity boxingTypeEntity() {
        return trainingTypeWithId(3L, TrainingType.BOXING);
    }

    public static TrainingTypeEntity pilatesTypeEntity() {
        return trainingTypeWithId(4L, TrainingType.PILATES);
    }

    public static TrainingTypeEntity trainingTypeWithId(long id, TrainingType typeName) {
        TrainingTypeEntity entity = trainingType(typeName);
        entity.setId(id);
        return entity;
    }

    public static UserEntity user(
            Long id,
            String firstName,
            String lastName,
            String username,
            String password,
            boolean active) {
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUsername(username);
        user.setPassword(password);
        user.setActive(active);
        return user;
    }

    public static TraineeEntity seedTraineeAliceWalker() {
        TraineeEntity trainee = new TraineeEntity();
        trainee.setId(4L);
        trainee.setUser(user(4L, "Alice", "Walker", "Alice.Walker", "qW3eRt5yUi", true));
        trainee.setDateOfBirth(LocalDate.of(1995, 4, 12));
        trainee.setAddress("123 Main St");
        return trainee;
    }

    public static TrainerEntity seedTrainerJohnSmith() {
        TrainerEntity trainer = new TrainerEntity();
        trainer.setId(1L);
        trainer.setUser(user(1L, "John", "Smith", "John.Smith", "pass1234AB", true));
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
        trainer.setUser(user(null, "John", "Smith", null, null, true));
        trainer.setSpecialization(yogaTypeEntity());
        return trainer;
    }

    public static TrainerEntity createTrainerWithCredentials() {
        TrainerEntity trainer = createDefaultTrainer();
        trainer.getUser().setUsername("John.Smith");
        trainer.getUser().setPassword("secret1234");
        trainer.getUser().setActive(true);
        return trainer;
    }

    public static TrainerEntity trainerWithId(long id, String username) {
        TrainerEntity trainer = createTrainerWithCredentials();
        trainer.getUser().setId(id);
        trainer.setId(id);
        trainer.getUser().setUsername(username);
        return trainer;
    }

    public static TrainerEntity trainer(String username) {
        TrainerEntity trainer = createTrainerWithCredentials();
        trainer.getUser().setUsername(username);
        return trainer;
    }

    public static TraineeEntity createDefaultTrainee() {
        TraineeEntity trainee = new TraineeEntity();
        trainee.setUser(user(null, "Alice", "Walker", null, null, true));
        trainee.setDateOfBirth(LocalDate.of(1995, 4, 12));
        trainee.setAddress("Kyiv");
        return trainee;
    }

    public static TraineeEntity createTraineeWithCredentials() {
        TraineeEntity trainee = createDefaultTrainee();
        trainee.getUser().setUsername("Alice.Walker");
        trainee.getUser().setPassword("secret1234");
        trainee.getUser().setActive(true);
        return trainee;
    }

    public static TraineeEntity traineeWithId(long id, String username) {
        TraineeEntity trainee = createTraineeWithCredentials();
        trainee.getUser().setId(id);
        trainee.setId(id);
        trainee.getUser().setUsername(username);
        return trainee;
    }

    public static TraineeEntity trainee(String username) {
        TraineeEntity trainee = createTraineeWithCredentials();
        trainee.getUser().setUsername(username);
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
                trainingTypeResponse(1L, TrainingType.YOGA));
    }

    public static CreateTrainerRequest createTrainerRequest(TrainingType specialization) {
        return new CreateTrainerRequest(new UserInfo("John", "Smith"), specialization);
    }

    public static UpdateTrainerRequest updateTrainerRequest(long id, TrainingType specialization) {
        return new UpdateTrainerRequest(id, new UserInfo("John", "Smith"), true, specialization);
    }

    public static TrainingTypeResponse trainingTypeResponse(long id, TrainingType typeName) {
        return new TrainingTypeResponse(id, typeName);
    }

    public static ScheduleTrainingRequest scheduleTrainingRequest(
            long traineeId, long trainerId, TrainingType type) {
        return scheduleTrainingRequest(traineeId, trainerId, type, LocalDate.of(2024, 3, 1));
    }

    public static ScheduleTrainingRequest scheduleTrainingRequest(
            long traineeId, long trainerId, TrainingType type, LocalDate date) {
        return new ScheduleTrainingRequest(
                traineeId,
                trainerId,
                "Morning Yoga",
                type,
                date,
                Duration.ofMinutes(60));
    }

    public static AutoScheduleTrainingRequest autoScheduleTrainingRequest(long traineeId, TrainingType type) {
        return autoScheduleTrainingRequest(traineeId, type, LocalDate.of(2024, 3, 1));
    }

    public static AutoScheduleTrainingRequest autoScheduleTrainingRequest(
            long traineeId, TrainingType type, LocalDate date) {
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
                trainingTypeResponse(1L, TrainingType.YOGA),
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
                TrainingType.YOGA,
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
