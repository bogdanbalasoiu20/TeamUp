
BEGIN;

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS password_changed_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS token_version INTEGER NOT NULL DEFAULT 0;

UPDATE users SET token_version = 0 WHERE token_version IS NULL;


COMMIT;