ALTER TABLE users
    ADD CONSTRAINT chk_users_position
        CHECK (position IN ('GOALKEEPER','DEFENDER','MIDFIELDER','FORWARD') OR position IS NULL);
