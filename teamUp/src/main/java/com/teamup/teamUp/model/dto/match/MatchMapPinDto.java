package com.teamup.teamUp.model.dto.match;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record MatchMapPinDto(
        UUID matchId,
        Double lat,
        Double lng,
        String title,
        Instant startsAt,
        Long currentPlayers,
        Integer maxPlayers,
        String venueName,
        Integer durationMinutes,
        BigDecimal totalPrice,
        String notes
) {}
