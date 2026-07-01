package com.epam.gymcrm.util;

import com.epam.gymcrm.entity.TraineeEntity;
import com.epam.gymcrm.entity.TrainerEntity;
import com.epam.gymcrm.entity.TrainingEntity;
import com.epam.gymcrm.model.TrainingType;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public final class CsvDataReader {

    private static final CsvMapper CSV_MAPPER = new CsvMapper()
            .enable(CsvParser.Feature.SKIP_EMPTY_LINES)
            .enable(CsvParser.Feature.TRIM_SPACES);

    private static final CsvSchema SCHEMA = CsvSchema.emptySchema().withHeader();

    private CsvDataReader() {
    }

    public static List<TrainerEntity> readTrainers(String resourcePath) throws IOException {
        return read(resourcePath, CsvDataReader::toTrainer);
    }

    public static List<TraineeEntity> readTrainees(String resourcePath) throws IOException {
        return read(resourcePath, CsvDataReader::toTrainee);
    }

    public static List<TrainingEntity> readTrainings(String resourcePath) throws IOException {
        return read(resourcePath, CsvDataReader::toTraining);
    }

    private static <T> List<T> read(String resourcePath, Function<Map<String, String>, T> mapper) throws IOException {
        try (InputStream input = openResource(resourcePath);
             MappingIterator<Map<String, String>> rows = CSV_MAPPER
                     .readerForMapOf(String.class)
                     .with(SCHEMA)
                     .readValues(input)) {
            List<T> result = new ArrayList<>();
            while (rows.hasNext()) {
                result.add(mapper.apply(rows.next()));
            }
            return result;
        }
    }

    private static TrainerEntity toTrainer(Map<String, String> row) {
        TrainerEntity trainer = new TrainerEntity();
        trainer.setUserId(Long.parseLong(row.get("id")));
        trainer.setFirstName(row.get("firstName"));
        trainer.setLastName(row.get("lastName"));
        trainer.setUsername(row.get("username"));
        trainer.setPassword(row.get("password"));
        trainer.setActive(Boolean.parseBoolean(row.get("active")));
        trainer.setSpecialization(TrainingType.valueOf(row.get("specialization").toUpperCase()));
        return trainer;
    }

    private static TraineeEntity toTrainee(Map<String, String> row) {
        TraineeEntity trainee = new TraineeEntity();
        trainee.setUserId(Long.parseLong(row.get("id")));
        trainee.setFirstName(row.get("firstName"));
        trainee.setLastName(row.get("lastName"));
        trainee.setUsername(row.get("username"));
        trainee.setPassword(row.get("password"));
        trainee.setActive(Boolean.parseBoolean(row.get("active")));
        trainee.setDateOfBirth(LocalDate.parse(row.get("dateOfBirth")));
        trainee.setAddress(row.get("address"));
        return trainee;
    }

    private static TrainingEntity toTraining(Map<String, String> row) {
        TrainingEntity training = new TrainingEntity();
        training.setTrainingId(Long.parseLong(row.get("id")));
        training.setTraineeId(Long.parseLong(row.get("traineeId")));
        training.setTrainerId(Long.parseLong(row.get("trainerId")));
        training.setTrainingName(row.get("trainingName"));
        training.setTrainingType(TrainingType.valueOf(row.get("trainingType").toUpperCase()));
        training.setTrainingDate(LocalDate.parse(row.get("trainingDate")));
        training.setTrainingDuration(Duration.ofMinutes(Long.parseLong(row.get("durationMinutes"))));
        return training;
    }

    private static InputStream openResource(String path) {
        return Objects.requireNonNull(
                CsvDataReader.class.getClassLoader().getResourceAsStream(path),
                () -> "Classpath resource not found: " + path);
    }
}
