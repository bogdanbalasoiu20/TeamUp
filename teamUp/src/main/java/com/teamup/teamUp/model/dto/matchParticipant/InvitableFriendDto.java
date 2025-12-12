package com.teamup.teamUp.model.dto.matchParticipant;

import java.util.UUID;

public record InvitableFriendDto(
        UUID userId,
        String username,
        boolean invited
) {
}
