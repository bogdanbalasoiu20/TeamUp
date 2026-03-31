package com.teamup.teamUp.model.dto.dashboard;

import java.time.LocalDateTime;
import java.util.UUID;

public record UpcomingTournamentDto(
        UUID id,
        String name,
        LocalDateTime startsAt,
        String teamName
) {}
