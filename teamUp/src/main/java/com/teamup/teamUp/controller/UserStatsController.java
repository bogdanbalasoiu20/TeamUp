package com.teamup.teamUp.controller;

import com.teamup.teamUp.model.dto.user.UserProfileResponseDto;
import com.teamup.teamUp.model.dto.user.UserStatsDto;
import com.teamup.teamUp.service.UserStatsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserStatsController {
    private final UserStatsService userStatsService;

    public UserStatsController(UserStatsService userStatsService) {
        this.userStatsService = userStatsService;
    }

    @GetMapping("/{userId}/stats")
    public ResponseEntity<ResponseApi<UserStatsDto>> getUserStats(@PathVariable UUID userId) {
        return ResponseEntity.ok(new ResponseApi<>("Stats fetched successfully", userStatsService.getUserStats(userId),true));
    }
}

