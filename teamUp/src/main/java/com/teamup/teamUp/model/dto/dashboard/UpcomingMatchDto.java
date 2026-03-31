package com.teamup.teamUp.model.dto.dashboard;

import java.time.Instant;
import java.util.UUID;

public record UpcomingMatchDto(
        UUID id,
        String title,
        Instant startsAt,
        String location,
        Integer currentPlayers,
        Integer maxPlayers
) {}
