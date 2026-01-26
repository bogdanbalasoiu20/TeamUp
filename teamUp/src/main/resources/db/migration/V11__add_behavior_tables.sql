CREATE TABLE player_behavior_stats (
       id UUID PRIMARY KEY,
       user_id UUID NOT NULL,

       fair_play DOUBLE PRECISION NOT NULL,
       competitiveness DOUBLE PRECISION NOT NULL,
       communication DOUBLE PRECISION NOT NULL,
       fun DOUBLE PRECISION NOT NULL,
       adaptability DOUBLE PRECISION NOT NULL,
       reliability DOUBLE PRECISION NOT NULL,

       feedback_count INTEGER NOT NULL,
       created_at TIMESTAMP NOT NULL,
       updated_at TIMESTAMP NOT NULL,

       CONSTRAINT uk_behavior_user UNIQUE (user_id),
       CONSTRAINT fk_behavior_user
           FOREIGN KEY (user_id)
               REFERENCES users(id)
               ON DELETE CASCADE
);


CREATE TABLE player_behavior_ratings (
     match_id UUID NOT NULL,
     rater_user_id UUID NOT NULL,
     rated_user_id UUID NOT NULL,

     fair_play SMALLINT,
     competitiveness SMALLINT,
     communication SMALLINT,
     fun SMALLINT,
     adaptability SMALLINT,
     reliability SMALLINT,

     created_at TIMESTAMP NOT NULL,

     CONSTRAINT pk_player_behavior_ratings
         PRIMARY KEY (match_id, rater_user_id, rated_user_id),

     CONSTRAINT fk_pbr_match
         FOREIGN KEY (match_id)
             REFERENCES matches(id)
             ON DELETE CASCADE,

     CONSTRAINT fk_pbr_rater
         FOREIGN KEY (rater_user_id)
             REFERENCES users(id)
             ON DELETE CASCADE,

     CONSTRAINT fk_pbr_rated
         FOREIGN KEY (rated_user_id)
             REFERENCES users(id)
             ON DELETE CASCADE
);
