package com.teamup.teamUp.model.dto.dashboard;

import com.teamup.teamUp.model.enums.Position;

public record UserHomeStatsDto(
        int rating,
        Position position,
        String avatarUrl,
        int ratingChange
) {}
