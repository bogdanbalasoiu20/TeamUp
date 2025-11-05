DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'mp_status') THEN
CREATE TYPE mp_status AS ENUM (
      'REQUESTED','ACCEPTED','DECLINED','LEFT','INVITED','KICKED','WAITLIST'
    );
END IF;
END $$;

ALTER TABLE match_participants
ALTER COLUMN status TYPE mp_status USING status::mp_status;