INSERT INTO training_types (id, type_name) VALUES (1, 'YOGA')
ON DUPLICATE KEY UPDATE type_name = VALUES(type_name);
INSERT INTO training_types (id, type_name) VALUES (2, 'CROSSFIT')
ON DUPLICATE KEY UPDATE type_name = VALUES(type_name);
INSERT INTO training_types (id, type_name) VALUES (3, 'BOXING')
ON DUPLICATE KEY UPDATE type_name = VALUES(type_name);
INSERT INTO training_types (id, type_name) VALUES (4, 'PILATES')
ON DUPLICATE KEY UPDATE type_name = VALUES(type_name);

INSERT INTO users (id, first_name, last_name, username, password, is_active)
VALUES (1, 'John', 'Smith', 'John.Smith', 'pass1234AB', TRUE)
ON DUPLICATE KEY UPDATE
    first_name = VALUES(first_name),
    last_name = VALUES(last_name),
    username = VALUES(username),
    password = VALUES(password),
    is_active = VALUES(is_active);
INSERT INTO trainers (id, specialization_id) VALUES (1, 1)
ON DUPLICATE KEY UPDATE specialization_id = VALUES(specialization_id);

INSERT INTO users (id, first_name, last_name, username, password, is_active)
VALUES (2, 'Anna', 'Jones', 'Anna.Jones', 'xK9mPqWz1L', TRUE)
ON DUPLICATE KEY UPDATE
    first_name = VALUES(first_name),
    last_name = VALUES(last_name),
    username = VALUES(username),
    password = VALUES(password),
    is_active = VALUES(is_active);
INSERT INTO trainers (id, specialization_id) VALUES (2, 2)
ON DUPLICATE KEY UPDATE specialization_id = VALUES(specialization_id);

INSERT INTO users (id, first_name, last_name, username, password, is_active)
VALUES (3, 'Mike', 'Brown', 'Mike.Brown', 'Tz7nVbCx4R', FALSE)
ON DUPLICATE KEY UPDATE
    first_name = VALUES(first_name),
    last_name = VALUES(last_name),
    username = VALUES(username),
    password = VALUES(password),
    is_active = VALUES(is_active);
INSERT INTO trainers (id, specialization_id) VALUES (3, 3)
ON DUPLICATE KEY UPDATE specialization_id = VALUES(specialization_id);

INSERT INTO users (id, first_name, last_name, username, password, is_active)
VALUES (4, 'Alice', 'Walker', 'Alice.Walker', 'qW3eRt5yUi', TRUE)
ON DUPLICATE KEY UPDATE
    first_name = VALUES(first_name),
    last_name = VALUES(last_name),
    username = VALUES(username),
    password = VALUES(password),
    is_active = VALUES(is_active);
INSERT INTO trainees (id, date_of_birth, address) VALUES (4, '1995-04-12', '123 Main St')
ON DUPLICATE KEY UPDATE
    date_of_birth = VALUES(date_of_birth),
    address = VALUES(address);

INSERT INTO users (id, first_name, last_name, username, password, is_active)
VALUES (5, 'Bob', 'Taylor', 'Bob.Taylor', 'Lm6oPs8dFg', TRUE)
ON DUPLICATE KEY UPDATE
    first_name = VALUES(first_name),
    last_name = VALUES(last_name),
    username = VALUES(username),
    password = VALUES(password),
    is_active = VALUES(is_active);
INSERT INTO trainees (id, date_of_birth, address) VALUES (5, '1990-07-30', '456 Oak Ave')
ON DUPLICATE KEY UPDATE
    date_of_birth = VALUES(date_of_birth),
    address = VALUES(address);

INSERT INTO users (id, first_name, last_name, username, password, is_active)
VALUES (6, 'Carol', 'White', 'Carol.White', 'Hn2jKx9cVb', FALSE)
ON DUPLICATE KEY UPDATE
    first_name = VALUES(first_name),
    last_name = VALUES(last_name),
    username = VALUES(username),
    password = VALUES(password),
    is_active = VALUES(is_active);
INSERT INTO trainees (id, date_of_birth, address) VALUES (6, '2000-01-15', '789 Pine Rd')
ON DUPLICATE KEY UPDATE
    date_of_birth = VALUES(date_of_birth),
    address = VALUES(address);

INSERT INTO trainee_trainer (trainee_id, trainer_id) VALUES (4, 1)
ON DUPLICATE KEY UPDATE trainee_id = VALUES(trainee_id);
INSERT INTO trainee_trainer (trainee_id, trainer_id) VALUES (5, 2)
ON DUPLICATE KEY UPDATE trainee_id = VALUES(trainee_id);

INSERT INTO trainings (id, trainee_id, trainer_id, training_name, training_type_id, training_date, training_duration)
VALUES (1, 4, 1, 'Morning Yoga', 1, '2024-03-01', 60)
ON DUPLICATE KEY UPDATE
    trainee_id = VALUES(trainee_id),
    trainer_id = VALUES(trainer_id),
    training_name = VALUES(training_name),
    training_type_id = VALUES(training_type_id),
    training_date = VALUES(training_date),
    training_duration = VALUES(training_duration);
INSERT INTO trainings (id, trainee_id, trainer_id, training_name, training_type_id, training_date, training_duration)
VALUES (2, 5, 2, 'CrossFit Intro', 2, '2024-03-02', 90)
ON DUPLICATE KEY UPDATE
    trainee_id = VALUES(trainee_id),
    trainer_id = VALUES(trainer_id),
    training_name = VALUES(training_name),
    training_type_id = VALUES(training_type_id),
    training_date = VALUES(training_date),
    training_duration = VALUES(training_duration);
INSERT INTO trainings (id, trainee_id, trainer_id, training_name, training_type_id, training_date, training_duration)
VALUES (3, 4, 3, 'Boxing Basics', 3, '2024-03-03', 45)
ON DUPLICATE KEY UPDATE
    trainee_id = VALUES(trainee_id),
    trainer_id = VALUES(trainer_id),
    training_name = VALUES(training_name),
    training_type_id = VALUES(training_type_id),
    training_date = VALUES(training_date),
    training_duration = VALUES(training_duration);

ALTER TABLE users AUTO_INCREMENT = 7;
ALTER TABLE training_types AUTO_INCREMENT = 5;
ALTER TABLE trainings AUTO_INCREMENT = 4;
