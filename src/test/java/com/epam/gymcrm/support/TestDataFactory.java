package com.epam.gymcrm.support;

import com.epam.gymcrm.dto.TraineeDto;
import com.epam.gymcrm.dto.TrainerDto;
import com.epam.gymcrm.dto.TrainingDto;
import com.epam.gymcrm.dto.TrainingTypeDto;
import com.epam.gymcrm.entity.TraineeEntity;
import com.epam.gymcrm.entity.TrainerEntity;
import com.epam.gymcrm.entity.TrainingEntity;
import com.epam.gymcrm.entity.TrainingTypeEntity;
import com.epam.gymcrm.security.Credentials;

import java.time.LocalDate;

public final class TestDataFactory {

    private TestDataFactory() {
    }

    public static TrainingTypeDto yogaTypeDto() {
        return TrainingTypeDto.builder().id(1L).typeName("YOGA").build();
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

    public static TraineeDto traineeDto() {
        return TraineeDto.builder()
                .firstName("Jane")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(1998, 5, 20))
                .address("Kyiv")
                .build();
    }

    public static TraineeDto traineeDtoWithCredentials(long id, String username) {
        return TraineeDto.builder()
                .id(id)
                .firstName("Alice")
                .lastName("Walker")
                .username(username)
                .password("secret1234")
                .active(true)
                .dateOfBirth(LocalDate.of(1995, 4, 12))
                .address("Kyiv")
                .build();
    }

    public static TrainerDto trainerDto() {
        return TrainerDto.builder()
                .firstName("John")
                .lastName("Smith")
                .specialization(yogaTypeDto())
                .build();
    }

    public static TrainerDto trainerDtoWithCredentials(long id, String username) {
        return TrainerDto.builder()
                .id(id)
                .firstName("John")
                .lastName("Smith")
                .username(username)
                .password("secret1234")
                .active(true)
                .specialization(yogaTypeDto())
                .build();
    }

    public static TrainingDto trainingDto(long traineeId, long trainerId) {
        return TrainingDto.builder()
                .trainee(TraineeDto.builder().id(traineeId).build())
                .trainer(TrainerDto.builder().id(trainerId).build())
                .trainingName("Morning Yoga")
                .trainingType(yogaTypeDto())
                .trainingDate(LocalDate.of(2024, 3, 1))
                .trainingDuration(60)
                .build();
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
        training.setTrainingDuration(60);
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

    public static Credentials credentialsOf(TraineeDto trainee) {
        return new Credentials(trainee.getUsername(), trainee.getPassword());
    }

    public static Credentials credentialsOf(TrainerDto trainer) {
        return new Credentials(trainer.getUsername(), trainer.getPassword());
    }

    public static Credentials aliceCredentials() {
        return new Credentials("Alice.Walker", "qW3eRt5yUi");
    }

    public static Credentials johnSmithCredentials() {
        return new Credentials("John.Smith", "pass1234AB");
    }
}
