package com.teamup.teamUp.model.dto.tournament;

import com.teamup.teamUp.model.enums.MatchStatus;

import java.util.UUID;

public record TournamentMatchResponseDto(
        UUID id,
        UUID homeTeamId,
        String homeTeamName,
        UUID awayTeamId,
        String awayTeamName,

        Integer scoreHome,
        Integer scoreAway,

        MatchStatus status,
        Integer matchDay
) {
}
