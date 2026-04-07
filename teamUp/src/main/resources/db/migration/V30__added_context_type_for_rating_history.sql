DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'event_type') THEN
CREATE TYPE event_type AS ENUM ('OPEN_MATCH', 'TOURNAMENT_MATCH');
END IF;
END$$;


ALTER TABLE player_card_stats_history
    ADD COLUMN event_type event_type;


ALTER TABLE player_card_stats_history
    ADD COLUMN context_id UUID;


UPDATE player_card_stats_history
SET event_type = 'OPEN_MATCH',
    context_id = match_id
WHERE match_id IS NOT NULL;


ALTER TABLE tournaments
DROP COLUMN rating_opened_at;

ALTER TABLE tournaments
DROP COLUMN rating_finalized;


ALTER TABLE player_ratings
DROP COLUMN context_id;

ALTER TABLE player_ratings
DROP COLUMN context_type;