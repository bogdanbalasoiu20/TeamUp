package com.teamup.teamUp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamup.teamUp.exceptions.ForbiddenException;
import com.teamup.teamUp.exceptions.NotFoundException;
import com.teamup.teamUp.mapper.NotificationMapper;
import com.teamup.teamUp.model.dto.notification.NotificationResponseDto;
import com.teamup.teamUp.model.dto.notification.NotificationWebSocketDto;
import com.teamup.teamUp.model.entity.Notification;
import com.teamup.teamUp.model.entity.User;
import com.teamup.teamUp.model.enums.NotificationType;
import com.teamup.teamUp.repository.NotificationRepository;
import com.teamup.teamUp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository, SimpMessagingTemplate messagingTemplate) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public void send(User user, NotificationType type, String title, String body, Map<String,Object> payload) {

        var notification = Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .body(body)
                .payload(payload)
                .isSeen(false)
                .createdAt(Instant.now())
                .build();

        notificationRepository.save(notification);

        try {
            messagingTemplate.convertAndSendToUser(
                    user.getId().toString(),
                    "/queue/notifications",
                    NotificationWebSocketDto.from(notification)
            );
        } catch (Exception ignored) {}
    }


    public Page<NotificationResponseDto> list(String username, Pageable pageable) {
        User user = userRepository.findByUsernameIgnoreCaseAndDeletedFalse(username).orElseThrow(()->new NotFoundException("User not found"));
        return notificationRepository.findByUserOrderByCreatedAtDesc(user, pageable).map(NotificationMapper::toDto);
    }

    public void markAsSeen(UUID id, String username){
        var user = userRepository.findByUsernameIgnoreCaseAndDeletedFalse(username).orElseThrow(()->new NotFoundException("User not found"));

        var notif = notificationRepository.findById(id).orElseThrow(()->new RuntimeException("Notification not found"));

        if(!notif.getUser().getId().equals(user.getId())){
            throw new ForbiddenException("Can not modify another user's notification");
        }

        notif.setIsSeen(true);
        notif.setSeenAt(Instant.now());
        notificationRepository.save(notif);
    }

    public long countUnseen(String username){
        User user = userRepository.findByUsernameIgnoreCaseAndDeletedFalse(username).orElseThrow(()->new NotFoundException("User not found"));
        return notificationRepository.countByUserAndIsSeenFalse(user);
    }
}
