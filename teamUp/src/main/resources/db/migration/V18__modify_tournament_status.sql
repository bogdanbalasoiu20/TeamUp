UPDATE tournaments
SET status = 'ONGOING'
WHERE status = 'STARTED';

ALTER TYPE tournament_status RENAME TO tournament_status_old;

CREATE TYPE tournament_status AS ENUM ('OPEN', 'ONGOING', 'FINISHED');

ALTER TABLE tournaments
ALTER COLUMN status TYPE tournament_status
    USING status::text::tournament_status;

DROP TYPE tournament_status_old;