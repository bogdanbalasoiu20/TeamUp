package com.teamup.teamUp.model.dto.friend;

import com.teamup.teamUp.model.enums.FriendRequestStatus;

import java.time.Instant;
import java.util.UUID;

public record FriendRequestResponseDto(
        UUID id,
        UUID requesterId,
        String requesterUsername,
        UUID addresseeId,
        String addresseeUsername,
        FriendRequestStatus status,
        String message,
        Instant createdAt,
        Instant respondedAt
) {
}
