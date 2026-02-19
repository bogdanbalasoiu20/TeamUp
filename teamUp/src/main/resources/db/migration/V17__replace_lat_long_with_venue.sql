ALTER TABLE tournaments
DROP COLUMN IF EXISTS latitude,
DROP COLUMN IF EXISTS longitude;

ALTER TABLE tournaments
ADD COLUMN venue_id UUID NOT NULL;

ALTER TABLE tournaments
ADD CONSTRAINT fk_tournament_venue FOREIGN KEY (venue_id) REFERENCES venues(id);