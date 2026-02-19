package com.teamup.teamUp.model.dto.tournament;

import java.util.UUID;

public record TournamentStandingResponseDto(
        UUID teamId,
        String teamName,
        int played,
        int wins,
        int draws,
        int losses,

        int goalsFor,
        int goalsAgainst,
        int points
) {
}
