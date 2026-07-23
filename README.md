# Gym CRM

Spring + Hibernate CRM for trainees, trainers, and training sessions.

## Requirements

- Java 21
- Maven 3.9+
- Docker

## Run

```bash
docker compose up -d
mvn exec:java
```

Stop database:

```bash
docker compose down
```

## Tests

```bash
docker compose up -d
mvn test
```

Integration tests use the same MySQL instance as the app (`gymdb` on port `3306`). Seed data from `docker/mysql/init/` must be present.

If schema validation fails (for example, `missing column [duration_minutes]`), reset the database volume and recreate it:

```bash
docker compose down -v
docker compose up -d
```

Or apply the migration manually:

```bash
docker exec -i gym-crm-mysql mysql -ugym_user -pgym_password gymdb < docker/mysql/init/03-migration-duration-minutes.sql
```

## Database

MySQL runs in Docker on port `3306`. Schema and seed data are applied on first container start from `docker/mysql/init/`.

| | |
|---|---|
| Database | `gymdb` |
| User | `gym_user` |
| Password | `gym_password` |

Connection settings: `src/main/resources/application.properties`.

## Structure

- `entity` — JPA entities
- `repository` — Hibernate `SessionFactory` repositories
- `service` — business logic
- `mapper` — MapStruct DTO mapping
- `config` — Spring and Hibernate configuration
