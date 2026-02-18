CREATE TYPE team_role AS ENUM ('CAPTAIN', 'PLAYER');
CREATE TYPE tournament_status AS ENUM ('OPEN', 'STARTED', 'FINISHED');


CREATE TABLE teams (
   id UUID PRIMARY KEY,
   name VARCHAR(255) NOT NULL UNIQUE,
   captain_id UUID NOT NULL,
   created_at TIMESTAMP NOT NULL,
   wins INTEGER DEFAULT 0,
   draws INTEGER DEFAULT 0,
   losses INTEGER DEFAULT 0,
   team_rating DOUBLE PRECISION,
   team_chemistry DOUBLE PRECISION,

   CONSTRAINT fk_team_captain FOREIGN KEY (captain_id) REFERENCES users(id) ON DELETE CASCADE
);


CREATE TABLE team_members (
  id UUID PRIMARY KEY,
  team_id UUID NOT NULL,
  user_id UUID NOT NULL,
  role team_role,
  joined_at TIMESTAMP,

  CONSTRAINT fk_team_member_team FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE CASCADE,
  CONSTRAINT fk_team_member_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT uk_team_user UNIQUE (team_id, user_id)
);


CREATE TABLE tournaments (
 id UUID PRIMARY KEY,
 name VARCHAR(255) NOT NULL,
 latitude DOUBLE PRECISION,
 longitude DOUBLE PRECISION,
 organizer_id UUID NOT NULL,
 max_teams INTEGER NOT NULL,
 status tournament_status,
 starts_at TIMESTAMP,
 ends_at TIMESTAMP,
 created_at TIMESTAMP,

 CONSTRAINT fk_tournament_organizer FOREIGN KEY (organizer_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE tournament_teams (
  id UUID PRIMARY KEY,
  tournament_id UUID NOT NULL,
  team_id UUID NOT NULL,
  joined_at TIMESTAMP,

  CONSTRAINT fk_tournament_team_tournament FOREIGN KEY (tournament_id)  REFERENCES tournaments(id) ON DELETE CASCADE,
  CONSTRAINT fk_tournament_team_team FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE CASCADE,
  CONSTRAINT uk_tournament_team UNIQUE (tournament_id, team_id)
);


CREATE TABLE tournament_matches (
    id UUID PRIMARY KEY,
    tournament_id UUID NOT NULL,
    home_team_id UUID,
    away_team_id UUID,
    winner_team_id UUID,
    score_home INTEGER,
    score_away INTEGER,
    status match_status,
    match_day INTEGER,

    CONSTRAINT fk_match_tournament FOREIGN KEY (tournament_id) REFERENCES tournaments(id) ON DELETE CASCADE,
    CONSTRAINT fk_match_home_team FOREIGN KEY (home_team_id) REFERENCES teams(id) ON DELETE SET NULL,
    CONSTRAINT fk_match_away_team FOREIGN KEY (away_team_id) REFERENCES teams(id) ON DELETE SET NULL,
    CONSTRAINT fk_match_winner_team FOREIGN KEY (winner_team_id) REFERENCES teams(id) ON DELETE SET NULL
);


CREATE TABLE tournament_standings (
  id UUID PRIMARY KEY,
  tournament_id UUID NOT NULL,
  team_id UUID NOT NULL,
  played INTEGER DEFAULT 0,
  wins INTEGER DEFAULT 0,
  draws INTEGER DEFAULT 0,
  losses INTEGER DEFAULT 0,
  goals_for INTEGER DEFAULT 0,
  goals_against INTEGER DEFAULT 0,
  points INTEGER DEFAULT 0,

  CONSTRAINT fk_standing_tournament FOREIGN KEY (tournament_id) REFERENCES tournaments(id) ON DELETE CASCADE,
  CONSTRAINT fk_standing_team FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE CASCADE,
  CONSTRAINT uk_standing UNIQUE (tournament_id, team_id)
);





