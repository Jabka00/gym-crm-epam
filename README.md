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
- `repository` — in-memory repositories backed by `Map`
- `service` - business logic (including credential generation)
- `storage` - typed in-memory storage beans, CSV seed loading via `BeanPostProcessor`

## Seed data

CSV file paths are configured in `src/main/resources/application.properties`.
Storage maps are populated at startup by `StorageSeedBeanPostProcessor`, which detects storage beans by type (`TrainerStorage`, `TraineeStorage`, `TrainingStorage`).
