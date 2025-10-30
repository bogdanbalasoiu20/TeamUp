package com.teamup.teamUp.model.dto.matchParticipant;

import java.util.UUID;

public record JoinResponseDto(
        UUID matchId,
        int participants,
        int maxPlayers
) {
}
