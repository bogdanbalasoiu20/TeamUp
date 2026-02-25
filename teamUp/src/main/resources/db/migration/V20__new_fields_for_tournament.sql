ALTER TABLE tournaments
ADD COLUMN description TEXT;

ALTER TABLE tournaments
ADD COLUMN players_per_team INTEGER NOT NULL DEFAULT 5;