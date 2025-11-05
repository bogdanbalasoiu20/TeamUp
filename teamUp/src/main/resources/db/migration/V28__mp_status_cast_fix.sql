DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'mp_status') THEN
CREATE TYPE mp_status AS ENUM
    ('REQUESTED','ACCEPTED','DECLINED','LEFT','INVITED','KICKED','WAITLIST');
END IF;
END $$;

DO $$
DECLARE r record;
BEGIN
FOR r IN
SELECT n.nspname, t.relname, c.conname
FROM pg_constraint c
         JOIN pg_class t ON t.oid = c.conrelid
         JOIN pg_namespace n ON n.oid = t.relnamespace
WHERE n.nspname = 'public'
  AND t.relname = 'match_participants'
  AND c.contype = 'c'
  AND pg_get_constraintdef(c.oid) ILIKE '%status%'
  LOOP
    EXECUTE format('ALTER TABLE %I.%I DROP CONSTRAINT %I', r.nspname, r.relname, r.conname);
END LOOP;
END $$;

ALTER TABLE public.match_participants
    ALTER COLUMN status DROP DEFAULT;

ALTER TABLE public.match_participants
ALTER COLUMN status TYPE mp_status USING status::mp_status;

ALTER TABLE public.match_participants
    ALTER COLUMN status SET DEFAULT 'REQUESTED'::mp_status;
