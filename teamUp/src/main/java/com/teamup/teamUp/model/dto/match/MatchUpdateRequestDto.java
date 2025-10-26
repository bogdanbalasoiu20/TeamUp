package com.teamup.teamUp.model.dto.match;

import com.teamup.teamUp.model.enums.MatchVisibility;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record MatchUpdateRequestDto(
        UUID venueId,
        @FutureOrPresent Instant startsAt,
        @Positive Integer durationMinutes,
        @Positive Integer maxPlayers,
        @Size(max=120) String title,
        @Size(max=2000) String notes,
        MatchVisibility visibility,
        @Future Instant joinDeadline,
        @Digits(integer=8, fraction=2) BigDecimal totalPrice,
        @NotNull Long version
) {}
