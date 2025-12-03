package com.teamup.teamUp.model.dto.matchParticipant;

import org.springframework.data.domain.Page;

import java.util.UUID;

public record MatchParticipantsResponse(
        UUID creatorId,
        Page<ParticipantDto> participants
) {
}
