ALTER TABLE player_behavior_stats
    ALTER COLUMN id SET DEFAULT gen_random_uuid();


INSERT INTO player_behavior_stats (
    user_id,
    fair_play,
    competitiveness,
    communication,
    fun,
    adaptability,
    reliability,
    feedback_count,
    created_at,
    updated_at
)
SELECT
    u.id,
    70,
    70,
    70,
    70,
    70,
    70,
    0,
    NOW(),
    NOW()
FROM users u
         LEFT JOIN player_behavior_stats pbs
                   ON pbs.user_id = u.id
WHERE pbs.user_id IS NULL;

