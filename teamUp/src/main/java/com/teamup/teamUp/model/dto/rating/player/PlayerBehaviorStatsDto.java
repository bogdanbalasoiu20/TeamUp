package com.teamup.teamUp.model.dto.rating.player;


public record PlayerBehaviorStatsDto(
        int fairPlay,
        int communication,
        int fun,
        int competitiveness,
        int selfishness,
        int aggressiveness,
        int feedbackCount
) {}

