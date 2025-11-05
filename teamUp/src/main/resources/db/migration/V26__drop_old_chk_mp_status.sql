ALTER TABLE match_participants
DROP CONSTRAINT IF EXISTS chk_mp_status;


ALTER TABLE match_participants
    ALTER COLUMN status SET DEFAULT 'REQUESTED'::mp_status;