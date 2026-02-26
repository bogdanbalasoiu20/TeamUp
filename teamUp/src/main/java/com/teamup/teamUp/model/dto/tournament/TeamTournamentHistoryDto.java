package com.teamup.teamUp.model.dto.tournament;

import java.util.UUID;

public record TeamTournamentHistoryDto(
        UUID tournamentId,
        String tournamentName,
        int finalPosition,
        int played,
        int wins,
        int draws,
        int losses,
        int goalsFor,
        int goalsAgainst
) {}
