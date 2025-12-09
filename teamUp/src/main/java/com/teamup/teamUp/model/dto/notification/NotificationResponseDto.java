package com.teamup.teamUp.model.dto.notification;

import com.teamup.teamUp.model.enums.NotificationType;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record NotificationResponseDto(
        UUID id,
        NotificationType type,
        String title,
        String body,
        Map<String,Object> payload,
        Boolean isSeen,
        Instant createdAt,
        Instant seenAt
) {
}



