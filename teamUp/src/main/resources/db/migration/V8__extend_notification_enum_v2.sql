alter type notification_type add value if not exists 'MATCH_RATING';

ALTER TABLE matches
    ADD COLUMN rating_opened_at TIMESTAMPTZ,
    ADD COLUMN ratings_finalized BOOLEAN NOT NULL DEFAULT FALSE;