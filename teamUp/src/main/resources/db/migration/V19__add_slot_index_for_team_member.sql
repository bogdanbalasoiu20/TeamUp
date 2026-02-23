ALTER TABLE team_members
    ADD COLUMN squad_type VARCHAR(20);

ALTER TABLE team_members
    ADD COLUMN slot_index INTEGER;

UPDATE team_members
SET squad_type = 'BENCH'
WHERE squad_type IS NULL;

WITH ranked AS (
    SELECT id,
           ROW_NUMBER() OVER (PARTITION BY team_id ORDER BY joined_at) - 1 AS rn
    FROM team_members
)
UPDATE team_members tm
SET slot_index = ranked.rn
    FROM ranked
WHERE tm.id = ranked.id;

ALTER TABLE team_members
    ALTER COLUMN squad_type SET NOT NULL;

ALTER TABLE team_members
    ALTER COLUMN slot_index SET NOT NULL;

ALTER TABLE team_members
    ADD CONSTRAINT unique_team_slot
        UNIQUE (team_id, squad_type, slot_index);