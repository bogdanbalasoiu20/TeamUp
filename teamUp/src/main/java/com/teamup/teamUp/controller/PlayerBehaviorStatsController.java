package com.teamup.teamUp.controller;

import com.teamup.teamUp.model.dto.rating.player.PlayerBehaviorStatsDto;
import com.teamup.teamUp.service.PlayerBehaviorStatsQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class PlayerBehaviorStatsController {
    private final PlayerBehaviorStatsQueryService queryService;

    public PlayerBehaviorStatsController(PlayerBehaviorStatsQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping("/{userId}/behavior-stats")
    public ResponseEntity<ResponseApi<PlayerBehaviorStatsDto>> getBehaviorStats(@PathVariable UUID userId) {
        var dto = queryService.getBehaviorStats(userId);
        return ResponseEntity.ok(new ResponseApi<>("Behavior stats fetched successfully", dto, true));
    }
}
