ALTER TABLE users ADD COLUMN is_deleted boolean;
UPDATE users SET is_deleted = false;
ALTER TABLE users ALTER COLUMN is_deleted SET DEFAULT false;
ALTER TABLE users ALTER COLUMN is_deleted SET NOT NULL;
