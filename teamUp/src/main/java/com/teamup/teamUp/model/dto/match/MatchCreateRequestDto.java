package com.teamup.teamUp.model.dto.match;

import com.teamup.teamUp.model.enums.MatchVisibility;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record MatchCreateRequestDto(
        @NotNull
        UUID venueId,

        @NotNull @FutureOrPresent
        Instant startsAt,

        @NotNull @Positive
        Integer durationMinutes,

        @NotNull @Positive
        Integer maxPlayers,

        @NotBlank @Size(max = 120)
        String title,

        @Size(max = 2000)
        String notes,

        MatchVisibility visibility,

        @Future
        Instant joinDeadline,

        @Digits(integer = 8, fraction = 2)
        BigDecimal totalPrice

) {
}
