package com.teamup.teamUp.model.dto.match;

import com.teamup.teamUp.model.dto.user.UserPreviewDto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
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
        String notes,
        List<UserPreviewDto> participantsPreview
) {}
