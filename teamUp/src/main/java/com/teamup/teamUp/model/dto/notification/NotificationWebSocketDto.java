package com.teamup.teamUp.model.dto.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamup.teamUp.model.entity.Notification;
import com.teamup.teamUp.model.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationWebSocketDto {
    private UUID id;
    private NotificationType type;
    private String title;
    private String body;
    private Instant createdAt;
    private Map<String,Object> payload;

    public static NotificationWebSocketDto from(Notification n) {
        Map<String,Object> payloadMap = null;
        try {
            if (n.getPayload() != null) {
                payloadMap = new ObjectMapper().readValue(n.getPayload(), HashMap.class);
            }
        } catch (Exception ignored) {}

        return new NotificationWebSocketDto(
                n.getId(),
                n.getType(),
                n.getTitle(),
                n.getBody(),
                n.getCreatedAt(),
                payloadMap
        );
    }
}
