CREATE INDEX idx_pr_rater_user ON player_ratings(rater_user_id);
CREATE INDEX idx_pr_rated_user ON player_ratings(rated_user_id);
CREATE INDEX idx_match_created_by ON matches(creator);
CREATE INDEX idx_match_player_user ON match_participants(user_id);
CREATE INDEX idx_stats_history_user ON player_card_stats_history(user_id);
