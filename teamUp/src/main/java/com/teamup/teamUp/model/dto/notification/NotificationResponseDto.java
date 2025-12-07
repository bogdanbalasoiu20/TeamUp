package com.teamup.teamUp.model.dto.notification;

import com.teamup.teamUp.model.enums.NotificationType;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponseDto(
        UUID id,
        NotificationType type,
        String title,
        String body,
        String payload,
        Boolean isSeen,
        Instant createdAt,
        Instant seenAt
) {
}



