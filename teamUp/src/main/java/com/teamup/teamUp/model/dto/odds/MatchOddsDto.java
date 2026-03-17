package com.teamup.teamUp.model.dto.odds;

public record MatchOddsDto(
        double homeWinProbability,
        double drawProbability,
        double awayWinProbability,
        double homeWinOdds,
        double drawOdds,
        double awayWinOdds
) {}