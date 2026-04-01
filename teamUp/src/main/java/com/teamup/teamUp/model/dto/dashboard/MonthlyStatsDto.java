package com.teamup.teamUp.model.dto.dashboard;

public record MonthlyStatsDto(
        long totalThisMonth,
        long totalLastMonth,
        double percentageChange,
        long openMatchesThisMonth,
        long tournamentsThisMonth
) {}