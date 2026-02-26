package com.teamup.teamUp.mapper;

import com.teamup.teamUp.model.dto.tournament.TournamentStandingResponseDto;
import com.teamup.teamUp.model.entity.TournamentStanding;

public class StandingMapper {
    public static TournamentStandingResponseDto toDto(TournamentStanding ts) {
        return new TournamentStandingResponseDto(
                ts.getTeam().getId(),
                ts.getTeam().getName(),
                ts.getPlayed(),
                ts.getWins(),
                ts.getDraws(),
                ts.getLosses(),
                ts.getGoalsFor(),
                ts.getGoalsAgainst(),
                ts.getPoints(),
                ts.getFinalPosition()
        );
    }
}
