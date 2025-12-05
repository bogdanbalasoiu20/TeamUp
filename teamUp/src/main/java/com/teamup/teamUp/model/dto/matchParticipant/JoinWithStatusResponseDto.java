package com.teamup.teamUp.model.dto.matchParticipant;

import com.teamup.teamUp.model.enums.MatchParticipantStatus;

import java.util.UUID;

public record JoinWithStatusResponseDto(
        UUID matchId,
        int participants,
        int maxPlayers,
        boolean IsWaitingList
) {
}
