package com.teamup.teamUp.model.dto.team;

import java.util.UUID;

public record TeamStatisticsResponseDto(
        UUID teamId,
        String teamName,
        int played,
        int wins,
        int draws,
        int losses,
        int goalsFor,
        int goalsAgainst,
        int tournamentsPlayed,
        int tournamentsWon
) {}
