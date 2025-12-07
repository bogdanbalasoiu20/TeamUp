package com.teamup.teamUp.mapper;

import com.teamup.teamUp.model.dto.notification.NotificationResponseDto;
import com.teamup.teamUp.model.entity.Notification;

public class NotificationMapper {
    public static NotificationResponseDto toDto(Notification notification) {
        return new NotificationResponseDto(
                notification.getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getBody(),
                notification.getPayload(),
                notification.getIsSeen(),
                notification.getCreatedAt(),
                notification.getSeenAt()
        );
    }
}
