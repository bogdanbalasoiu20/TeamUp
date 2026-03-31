package com.teamup.teamUp.model.dto.dashboard;

import java.util.List;

public record HomeUpcomingResponse(
        List<UpcomingMatchDto> matches,
        List<UpcomingTournamentDto> tournaments
) {}
