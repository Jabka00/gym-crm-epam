CREATE TABLE IF NOT EXISTS users (
    id              BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    first_name      VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100) NOT NULL,
    username        VARCHAR(100) NOT NULL UNIQUE,
    password        VARCHAR(255) NOT NULL,
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS training_types (
    id          BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    type_name   VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS trainees (
    id              BIGINT       NOT NULL PRIMARY KEY,
    date_of_birth   DATE,
    address         VARCHAR(255),
    CONSTRAINT fk_trainees_user FOREIGN KEY (id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS trainers (
    id                  BIGINT NOT NULL PRIMARY KEY,
    specialization_id   BIGINT NOT NULL,
    CONSTRAINT fk_trainers_user FOREIGN KEY (id) REFERENCES users (id),
    CONSTRAINT fk_trainers_specialization FOREIGN KEY (specialization_id) REFERENCES training_types (id)
);

CREATE TABLE IF NOT EXISTS trainee_trainer (
    trainee_id  BIGINT NOT NULL,
    trainer_id  BIGINT NOT NULL,
    PRIMARY KEY (trainee_id, trainer_id),
    CONSTRAINT fk_tt_trainee FOREIGN KEY (trainee_id) REFERENCES trainees (id),
    CONSTRAINT fk_tt_trainer FOREIGN KEY (trainer_id) REFERENCES trainers (id)
);

CREATE TABLE IF NOT EXISTS trainings (
    id                  BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    trainee_id          BIGINT       NOT NULL,
    trainer_id          BIGINT       NOT NULL,
    training_name       VARCHAR(200) NOT NULL,
    training_type_id    BIGINT       NOT NULL,
    training_date       DATE         NOT NULL,
    training_duration   INT          NOT NULL,
    CONSTRAINT fk_trainings_trainee FOREIGN KEY (trainee_id) REFERENCES trainees (id),
    CONSTRAINT fk_trainings_trainer FOREIGN KEY (trainer_id) REFERENCES trainers (id),
    CONSTRAINT fk_trainings_training_type FOREIGN KEY (training_type_id) REFERENCES training_types (id)
);
