package com.teamup.teamUp.controller;

import com.teamup.teamUp.model.dto.notification.NotificationResponseDto;
import com.teamup.teamUp.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<ResponseApi<?>> list(Authentication auth, Pageable pageable){
        Page<NotificationResponseDto> page = notificationService.list(auth.getName(), pageable);
        return ResponseEntity.ok(new ResponseApi<>("notifications fetched",page,true));
    }

    @PatchMapping("/{id}/seen")
    public ResponseEntity<ResponseApi<?>> markSeen(Authentication auth, @PathVariable UUID id){
        notificationService.markAsSeen(id, auth.getName());
        return ResponseEntity.ok(new ResponseApi<>("notifications marked as seen",null,true));
    }

    @GetMapping("/unseen-count")
    public ResponseEntity<ResponseApi<?>> unseenCount(Authentication auth){
        long count = notificationService.countUnseen(auth.getName());
        return ResponseEntity.ok(new ResponseApi<>("notifications unseen count",count,true));
    }
}
