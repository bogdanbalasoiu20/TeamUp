package com.teamup.teamUp.model.dto.dashboard;

public record HomeResponse(
        HomeUpcomingResponse  homeUpcomingResponse,
        MonthlyStatsDto monthlyStats
) {}
