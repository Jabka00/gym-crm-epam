-- Training types
MERGE INTO training_types (id, type_name) KEY (id) VALUES (1, 'YOGA');
MERGE INTO training_types (id, type_name) KEY (id) VALUES (2, 'CROSSFIT');
MERGE INTO training_types (id, type_name) KEY (id) VALUES (3, 'BOXING');
MERGE INTO training_types (id, type_name) KEY (id) VALUES (4, 'PILATES');

-- Trainers (users 1-3)
MERGE INTO users (id, first_name, last_name, username, password, is_active) KEY (id)
    VALUES (1, 'John', 'Smith', 'John.Smith', 'pass1234AB', TRUE);
MERGE INTO trainers (user_id, training_type_id) KEY (user_id) VALUES (1, 1);

MERGE INTO users (id, first_name, last_name, username, password, is_active) KEY (id)
    VALUES (2, 'Anna', 'Jones', 'Anna.Jones', 'xK9mPqWz1L', TRUE);
MERGE INTO trainers (user_id, training_type_id) KEY (user_id) VALUES (2, 2);

MERGE INTO users (id, first_name, last_name, username, password, is_active) KEY (id)
    VALUES (3, 'Mike', 'Brown', 'Mike.Brown', 'Tz7nVbCx4R', FALSE);
MERGE INTO trainers (user_id, training_type_id) KEY (user_id) VALUES (3, 3);

-- Trainees (users 4-6; JOINED inheritance requires unique user ids across all subtypes)
MERGE INTO users (id, first_name, last_name, username, password, is_active) KEY (id)
    VALUES (4, 'Alice', 'Walker', 'Alice.Walker', 'qW3eRt5yUi', TRUE);
MERGE INTO trainees (user_id, date_of_birth, address) KEY (user_id)
    VALUES (4, '1995-04-12', '123 Main St');

MERGE INTO users (id, first_name, last_name, username, password, is_active) KEY (id)
    VALUES (5, 'Bob', 'Taylor', 'Bob.Taylor', 'Lm6oPs8dFg', TRUE);
MERGE INTO trainees (user_id, date_of_birth, address) KEY (user_id)
    VALUES (5, '1990-07-30', '456 Oak Ave');

MERGE INTO users (id, first_name, last_name, username, password, is_active) KEY (id)
    VALUES (6, 'Carol', 'White', 'Carol.White', 'Hn2jKx9cVb', FALSE);
MERGE INTO trainees (user_id, date_of_birth, address) KEY (user_id)
    VALUES (6, '2000-01-15', '789 Pine Rd');

-- Trainings
MERGE INTO trainings (id, trainee_id, trainer_id, training_name, training_type_id, training_date, training_duration) KEY (id)
    VALUES (1, 4, 1, 'Morning Yoga', 1, '2024-03-01', 60);
MERGE INTO trainings (id, trainee_id, trainer_id, training_name, training_type_id, training_date, training_duration) KEY (id)
    VALUES (2, 5, 2, 'CrossFit Intro', 2, '2024-03-02', 90);
MERGE INTO trainings (id, trainee_id, trainer_id, training_name, training_type_id, training_date, training_duration) KEY (id)
    VALUES (3, 4, 3, 'Boxing Basics', 3, '2024-03-03', 45);

-- Reset identity sequences after explicit ids
ALTER TABLE users ALTER COLUMN id RESTART WITH 7;
ALTER TABLE training_types ALTER COLUMN id RESTART WITH 5;
ALTER TABLE trainings ALTER COLUMN id RESTART WITH 4;
