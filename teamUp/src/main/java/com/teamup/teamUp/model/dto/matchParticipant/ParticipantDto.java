package com.teamup.teamUp.model.dto.matchParticipant;

import com.teamup.teamUp.model.enums.MatchParticipantStatus;

import java.time.Instant;
import java.util.UUID;

public record ParticipantDto(
        UUID userId,
        String username,
        MatchParticipantStatus status,
        Boolean bringsBall,
        Instant joinedAt
) {
}
