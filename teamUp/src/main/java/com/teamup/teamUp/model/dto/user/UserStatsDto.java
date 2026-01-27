package com.teamup.teamUp.model.dto.user;

public record UserStatsDto(
        int matchesPlayed,
        int matchesCreated,
        int votesGiven,
        int votesReceived,
        double currentRating,
        double maxRating
) {
}
