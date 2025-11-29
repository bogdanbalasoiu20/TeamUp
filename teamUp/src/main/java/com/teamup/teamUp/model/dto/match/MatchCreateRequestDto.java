package com.teamup.teamUp.model.dto.match;

import com.teamup.teamUp.model.enums.MatchVisibility;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record MatchCreateRequestDto(
        @NotNull(message = "Select a venue")
        UUID venueId,

        @NotNull(message = "Start time is required.")
        @FutureOrPresent(message = "Start time must be in the present or future.")
        Instant startsAt,

        @NotNull(message = "Duration is required.")
        @Positive(message = "Duration must be a positive number.")
        Integer durationMinutes,

        @NotNull(message = "Max players is required.")
        @Positive(message = "Max players must be a positive number.")
        Integer maxPlayers,

        @NotBlank(message = "Title is required.")
        @Size(max = 120,  message = "Title cannot exceed 120 characters.")
        String title,

        @Size(max = 2000, message = "Notes cannot exceed 2000 characters.")
        String notes,

        MatchVisibility visibility,

        @Future(message = "Join deadline must be in the future.")
        Instant joinDeadline,

        @Digits(integer = 8, fraction = 2,
                message = "Total price must be a valid amount (up to 2 decimals).")
        BigDecimal totalPrice

) {
}
