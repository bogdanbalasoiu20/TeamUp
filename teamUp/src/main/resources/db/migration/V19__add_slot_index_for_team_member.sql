ALTER TABLE team_members
    ADD COLUMN squad_type VARCHAR(20);

ALTER TABLE team_members
    ADD COLUMN slot_index INTEGER;

UPDATE team_members
SET squad_type = 'BENCH'
WHERE squad_type IS NULL;

UPDATE team_members
SET slot_index = 0
WHERE slot_index IS NULL;

ALTER TABLE team_members
    ALTER COLUMN squad_type SET NOT NULL;

ALTER TABLE team_members
    ALTER COLUMN slot_index SET NOT NULL;

ALTER TABLE team_members
    ADD CONSTRAINT unique_team_slot
        UNIQUE (team_id, squad_type, slot_index);