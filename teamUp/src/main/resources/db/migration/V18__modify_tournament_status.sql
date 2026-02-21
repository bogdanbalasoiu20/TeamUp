ALTER TYPE tournament_status RENAME TO tournament_status_old;

CREATE TYPE tournament_status AS ENUM ('OPEN', 'ONGOING', 'FINISHED');

ALTER TABLE tournaments
ALTER COLUMN status TYPE tournament_status
USING (
    CASE status::text
        WHEN 'STARTED' THEN 'ONGOING'
        ELSE status::text
    END
)::tournament_status;

DROP TYPE tournament_status_old;