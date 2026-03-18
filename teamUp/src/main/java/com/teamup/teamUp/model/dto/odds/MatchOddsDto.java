package com.teamup.teamUp.model.dto.odds;

public record MatchOddsDto(
        Double homeWinProbability,
        Double drawProbability,
        Double awayWinProbability,
        Double homeWinOdds,
        Double drawOdds,
        Double awayWinOdds
) {}