INSERT INTO player_behavior_stats (
    user_id,
    fair_play,
    communication,
    fun,
    competitiveness,
    adaptability,
    reliability,
    feedback_count
)
SELECT
    u.id,
    70,
    70,
    70,
    70,
    70,
    70,
    0
FROM users u
         LEFT JOIN player_behavior_stats pbs
                   ON pbs.user_id = u.id
WHERE pbs.user_id IS NULL;
