CREATE TABLE player_chemistry (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_a UUID NOT NULL,
  user_b UUID NOT NULL,
  chemistry_score INT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT now(),
  updated_at TIMESTAMP NOT NULL DEFAULT now(),

  CONSTRAINT fk_player_chem_a FOREIGN KEY (user_a) REFERENCES users(id),
  CONSTRAINT fk_player_chem_b FOREIGN KEY (user_b) REFERENCES users(id),
  CONSTRAINT unique_pair UNIQUE (user_a, user_b)
);


CREATE INDEX idx_player_chem_user_a ON player_chemistry(user_a);
CREATE INDEX idx_player_chem_user_b ON player_chemistry(user_b);