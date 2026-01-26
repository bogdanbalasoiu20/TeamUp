package com.teamup.teamUp.model.dto.rating;

import java.util.UUID;

public record PlayerBehaviorRatingDto(
        UUID ratedUserId,
        Integer fairPlay,
        Integer communication,
        Integer fun,
        Integer competitiveness,
        Integer adaptability,
        Integer reliability
) {}
