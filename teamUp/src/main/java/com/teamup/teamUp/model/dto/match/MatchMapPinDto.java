package com.teamup.teamUp.model.dto.match;

import java.time.Instant;
import java.util.UUID;

public record MatchMapPinDto(
        UUID matchId,
        double lat,
        double lng,
        String title,
        Instant startsAt,
        Integer currentPlayers,
        Integer maxPlayers,
        String venueName

) {
}
