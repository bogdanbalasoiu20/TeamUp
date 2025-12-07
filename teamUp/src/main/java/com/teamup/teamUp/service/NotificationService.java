package com.teamup.teamUp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamup.teamUp.exceptions.ForbiddenException;
import com.teamup.teamUp.exceptions.NotFoundException;
import com.teamup.teamUp.mapper.NotificationMapper;
import com.teamup.teamUp.model.dto.notification.NotificationResponseDto;
import com.teamup.teamUp.model.entity.Notification;
import com.teamup.teamUp.model.entity.User;
import com.teamup.teamUp.model.enums.NotificationType;
import com.teamup.teamUp.repository.NotificationRepository;
import com.teamup.teamUp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final ObjectMapper mapper;
    private final UserRepository userRepository;

    @Autowired
    public NotificationService(NotificationRepository notificationRepository, ObjectMapper mapper, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.mapper = mapper;
        this.userRepository = userRepository;
    }

    public void send(User user, NotificationType type, String title, String body, Map<String,Object> payload) {
        String payloadJson = null;

        if(payload !=null){
            try{
                payloadJson = mapper.writeValueAsString(payload);
            }catch(JsonProcessingException e){
                throw new ForbiddenException("Failed to serialize payload");
            }
        }

        var notification = Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .body(body)
                .payload(payloadJson)
                .isSeen(false)
                .createdAt(Instant.now())
                .build();

        notificationRepository.save(notification);
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
