ALTER TABLE player_behavior_stats
    RENAME COLUMN adaptability TO selfishness;

ALTER TABLE player_behavior_stats
    RENAME COLUMN reliability TO aggressiveness;


ALTER TABLE player_behavior_ratings
    RENAME COLUMN adaptability TO selfishness;

ALTER TABLE player_behavior_ratings
    RENAME COLUMN reliability TO aggressiveness;
