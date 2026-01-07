package com.teamup.teamUp.controller;

import com.teamup.teamUp.model.entity.PlayerCardStats;
import com.teamup.teamUp.model.entity.PlayerCardStatsHistory;
import com.teamup.teamUp.service.PlayerCardQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class PlayerCardController {

    private final PlayerCardQueryService playerCardQueryService;

    public PlayerCardController(PlayerCardQueryService playerCardQueryService) {
        this.playerCardQueryService = playerCardQueryService;
    }

    //cardul live al userului
    @GetMapping("/{userId}/card")
    public ResponseEntity<ResponseApi<PlayerCardStats>> getPlayerCard(@PathVariable UUID userId) {
        var card = playerCardQueryService.getLiveCard(userId);
        return ResponseEntity.ok(new ResponseApi<>("Player card fetched successfully", card, true));
    }

    //istoricul cardului unui user(pt grafice)
    @GetMapping("/{userId}/card/history")
    public ResponseEntity<ResponseApi<List<PlayerCardStatsHistory>>> getPlayerCardHistory(@PathVariable UUID userId) {
        var history = playerCardQueryService.getCardHistory(userId);
        return ResponseEntity.ok(new ResponseApi<>("Player card history fetched successfully", history, true));
    }
}
