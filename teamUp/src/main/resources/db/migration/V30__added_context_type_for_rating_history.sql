CREATE TYPE event_type AS ENUM ('OPEN_MATCH', 'TOURNAMENT_MATCH');

ALTER TABLE player_card_stats_history
ADD COLUMN event_type event_type;

ALTER TABLE player_card_stats_history
ADD COLUMN context_id UUID;

UPDATE player_card_stats_history
SET event_type = 'OPEN_MATCH',
    context_id = match_id
WHERE match_id IS NOT NULL;