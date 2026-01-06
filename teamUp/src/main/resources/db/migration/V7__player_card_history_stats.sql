ALTER TABLE player_ratings
DROP COLUMN pace,
    DROP COLUMN shooting,
    DROP COLUMN passing,
    DROP COLUMN defending,
    DROP COLUMN dribbling,
    DROP COLUMN physical;

ALTER TABLE player_ratings
    ADD COLUMN pace SMALLINT,
    ADD COLUMN shooting SMALLINT,
    ADD COLUMN passing SMALLINT,
    ADD COLUMN defending SMALLINT,
    ADD COLUMN dribbling SMALLINT,
    ADD COLUMN physical SMALLINT,

    ADD COLUMN gk_diving SMALLINT,
    ADD COLUMN gk_handling SMALLINT,
    ADD COLUMN gk_kicking SMALLINT,
    ADD COLUMN gk_reflexes SMALLINT,
    ADD COLUMN gk_speed SMALLINT,
    ADD COLUMN gk_positioning SMALLINT;

ALTER TABLE player_ratings
    ADD CONSTRAINT chk_pr_pace CHECK (pace BETWEEN 0 AND 99 OR pace IS NULL),
    ADD CONSTRAINT chk_pr_shooting CHECK (shooting BETWEEN 0 AND 99 OR shooting IS NULL),
    ADD CONSTRAINT chk_pr_passing CHECK (passing BETWEEN 0 AND 99 OR passing IS NULL),
    ADD CONSTRAINT chk_pr_defending CHECK (defending BETWEEN 0 AND 99 OR defending IS NULL),
    ADD CONSTRAINT chk_pr_dribbling CHECK (dribbling BETWEEN 0 AND 99 OR dribbling IS NULL),
    ADD CONSTRAINT chk_pr_physical CHECK (physical BETWEEN 0 AND 99 OR physical IS NULL),

    ADD CONSTRAINT chk_pr_gk_diving CHECK (gk_diving BETWEEN 0 AND 99 OR gk_diving IS NULL),
    ADD CONSTRAINT chk_pr_gk_handling CHECK (gk_handling BETWEEN 0 AND 99 OR gk_handling IS NULL),
    ADD CONSTRAINT chk_pr_gk_kicking CHECK (gk_kicking BETWEEN 0 AND 99 OR gk_kicking IS NULL),
    ADD CONSTRAINT chk_pr_gk_reflexes CHECK (gk_reflexes BETWEEN 0 AND 99 OR gk_reflexes IS NULL),
    ADD CONSTRAINT chk_pr_gk_speed CHECK (gk_speed BETWEEN 0 AND 99 OR gk_speed IS NULL),
    ADD CONSTRAINT chk_pr_gk_positioning CHECK (gk_positioning BETWEEN 0 AND 99 OR gk_positioning IS NULL);

CREATE INDEX idx_player_ratings_rated_user
    ON player_ratings (rated_user_id);

CREATE INDEX idx_player_ratings_match
    ON player_ratings (match_id);



CREATE TABLE player_card_stats (
                                   user_id UUID PRIMARY KEY,

                                   pace DOUBLE PRECISION,
                                   shooting DOUBLE PRECISION,
                                   passing DOUBLE PRECISION,
                                   defending DOUBLE PRECISION,
                                   dribbling DOUBLE PRECISION,
                                   physical DOUBLE PRECISION,

                                   gk_diving DOUBLE PRECISION,
                                   gk_handling DOUBLE PRECISION,
                                   gk_kicking DOUBLE PRECISION,
                                   gk_reflexes DOUBLE PRECISION,
                                   gk_speed DOUBLE PRECISION,
                                   gk_positioning DOUBLE PRECISION,

                                   overall_rating DOUBLE PRECISION NOT NULL,
                                   last_updated TIMESTAMPTZ NOT NULL DEFAULT now(),

                                   CONSTRAINT fk_pcs_user FOREIGN KEY (user_id) REFERENCES users(id),

                                   CONSTRAINT chk_pcs_overall CHECK (overall_rating BETWEEN 0 AND 99)
);



CREATE TABLE player_card_stats_history (
                                           id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                           user_id UUID NOT NULL,
                                           match_id UUID NOT NULL,

                                           pace DOUBLE PRECISION,
                                           shooting DOUBLE PRECISION,
                                           passing DOUBLE PRECISION,
                                           defending DOUBLE PRECISION,
                                           dribbling DOUBLE PRECISION,
                                           physical DOUBLE PRECISION,

                                           gk_diving DOUBLE PRECISION,
                                           gk_handling DOUBLE PRECISION,
                                           gk_kicking DOUBLE PRECISION,
                                           gk_reflexes DOUBLE PRECISION,
                                           gk_speed DOUBLE PRECISION,
                                           gk_positioning DOUBLE PRECISION,

                                           overall_rating DOUBLE PRECISION NOT NULL,
                                           recorded_at TIMESTAMPTZ NOT NULL DEFAULT now(),

                                           CONSTRAINT fk_pcsh_user FOREIGN KEY (user_id) REFERENCES users(id),
                                           CONSTRAINT fk_pcsh_match FOREIGN KEY (match_id) REFERENCES matches(id),

                                           CONSTRAINT chk_pcsh_overall CHECK (overall_rating BETWEEN 0 AND 99)
);

CREATE INDEX idx_pcsh_user_time
    ON player_card_stats_history (user_id, recorded_at);

CREATE INDEX idx_pcsh_match
    ON player_card_stats_history (match_id);
