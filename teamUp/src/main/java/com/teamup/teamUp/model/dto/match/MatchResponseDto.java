package com.teamup.teamUp.model.dto.match;

import com.teamup.teamUp.model.entity.User;
import com.teamup.teamUp.model.entity.Venue;
import com.teamup.teamUp.model.enums.MatchStatus;
import com.teamup.teamUp.model.enums.MatchVisibility;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record MatchResponseDto(
       UUID id,
       UUID creatorId,
       UUID venueId,
       Instant startsAt,
       Instant endsAt,
       Integer durationMinutes,
       Integer maxPlayers,
       Integer currentPlayers,
       Instant joinDeadline,
       String title,
       String notes,
       MatchStatus status,
       MatchVisibility visibility,
       BigDecimal totalPrice,
       Instant createdAt,
       Instant updatedAt
) {

}
