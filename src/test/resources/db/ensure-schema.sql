-- Keeps integration-test DB in sync when an older Docker volume is reused.
ALTER TABLE trainings
    ADD COLUMN IF NOT EXISTS duration_minutes INT NOT NULL DEFAULT 60;
