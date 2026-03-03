CREATE TABLE tournament_match_participants (
       match_id UUID NOT NULL,
       user_id UUID NOT NULL,
       team_id UUID NOT NULL,

       PRIMARY KEY (match_id, user_id),

       CONSTRAINT fk_tmp_match FOREIGN KEY (match_id) REFERENCES tournament_matches(id) ON DELETE CASCADE,
       CONSTRAINT fk_tmp_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
       CONSTRAINT fk_tmp_team FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE CASCADE
);


CREATE INDEX idx_tmp_user ON tournament_match_participants(user_id);
CREATE INDEX idx_tmp_team ON tournament_match_participants(team_id);