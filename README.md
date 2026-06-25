# Gym CRM

Spring Core module for managing gym clients, trainers, and training sessions.

## Requirements

- Java 17+
- Maven 3.9+

## Build

```bash
mvn clean compile
```

## Run

```bash
mvn exec:java
```

Or after building:

```bash
mvn clean package
java -cp target/gym-crm-1.0.0-SNAPSHOT.jar com.epam.gymcrm.GymCrmApp
```

## Tests

```bash
mvn test
```

## Structure

- `model` — domain entities
- `repository` — in-memory repositories, each holding its own `Map`
- `service` - business logic (including credential generation)
- `storage` - CSV parsing (`StorageCsvSeeder`) and startup seeding (`StorageSeedBeanPostProcessor`)

## Seed data

CSV file paths are configured in `src/main/resources/application.properties`.
Repositories are populated at startup by `StorageSeedBeanPostProcessor`, which detects
repository beans by type (`TrainerRepository`, `TraineeRepository`, `TrainingRepository`),
parses the CSV files via `StorageCsvSeeder`, and calls each repository's `load(...)` method.
