package com.teamup.teamUp.model.dto.chat;

import java.time.Instant;
import java.util.UUID;

public record MessageResponseDto(
        UUID id,
        UUID matchId,
        UUID senderId,
        String senderUsername,
        String content,
        Instant createdAt
//        Instant deletedAt,
//        boolean deleted
) {
}
