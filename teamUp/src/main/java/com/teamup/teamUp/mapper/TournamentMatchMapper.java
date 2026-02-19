package com.teamup.teamUp.mapper;

import com.teamup.teamUp.model.dto.tournament.TournamentMatchResponseDto;
import com.teamup.teamUp.model.entity.TournamentMatch;

public class TournamentMatchMapper {
    public static TournamentMatchResponseDto toDto(TournamentMatch tm) {
        return new TournamentMatchResponseDto(
             tm.getId(),
             tm.getHomeTeam().getId(),
             tm.getHomeTeam().getName(),
             tm.getAwayTeam().getId(),
             tm.getAwayTeam().getName(),
             tm.getScoreHome(),
             tm.getScoreAway(),
             tm.getStatus(),
             tm.getMatchDay()
        );
    }
}
