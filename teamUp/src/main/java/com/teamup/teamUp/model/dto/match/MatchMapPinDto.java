package com.teamup.teamUp.model.dto.match;

import java.math.BigDecimal;
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
        String venueName,
        Integer durationMinutes,
        BigDecimal totalPrice,
        String notes
) {
}
