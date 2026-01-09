ALTER TABLE matches
    ADD COLUMN finish_prompt_sent BOOLEAN NOT NULL DEFAULT FALSE;

alter type notification_type add value if not exists 'MATCH_FINISH_CONFIRMATION';