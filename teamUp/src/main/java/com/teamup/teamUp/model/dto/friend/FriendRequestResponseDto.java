package com.teamup.teamUp.model.dto.friend;

import com.teamup.teamUp.model.enums.FriendRequestStatus;

import java.time.Instant;
import java.util.UUID;

public record FriendRequestResponseDto(
        UUID id,
        UUID requesterId,
        String requesterUsername,
        String requesterPhotoUrl,
        UUID addresseeId,
        String addresseeUsername,
        String addresseePhotoUrl,
        FriendRequestStatus status,
        String message,
        Instant createdAt,
        Instant respondedAt
) {
}
