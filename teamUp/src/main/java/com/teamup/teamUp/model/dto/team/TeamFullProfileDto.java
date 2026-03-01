package com.teamup.teamUp.model.dto.team;

import com.teamup.teamUp.model.dto.rating.team.TeamRatingDto;
import com.teamup.teamUp.model.dto.tournament.TeamTournamentHistoryDto;

import java.util.List;

public record TeamFullProfileDto(
        TeamResponseDto team,
        TeamStatisticsResponseDto statistics,
        List<TeamTournamentHistoryDto> tournamentHistory,
        TeamRatingDto rating
) {}
