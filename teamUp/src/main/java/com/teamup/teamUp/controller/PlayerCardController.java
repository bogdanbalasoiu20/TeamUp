package com.teamup.teamUp.controller;

import com.teamup.teamUp.model.dto.card.PlayerCardDto;
import com.teamup.teamUp.model.dto.card.PlayerCardHistoryPointDto;
import com.teamup.teamUp.model.dto.liveform.LiveFormDto;
import com.teamup.teamUp.model.entity.PlayerCardStats;
import com.teamup.teamUp.model.entity.PlayerCardStatsHistory;
import com.teamup.teamUp.service.LiveFormService;
import com.teamup.teamUp.service.PlayerCardQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class PlayerCardController {

    private final PlayerCardQueryService playerCardQueryService;
    private final LiveFormService liveFormService;

    public PlayerCardController(PlayerCardQueryService playerCardQueryService, LiveFormService liveFormService) {
        this.playerCardQueryService = playerCardQueryService;
        this.liveFormService = liveFormService;
    }

    //cardul live al userului
    @GetMapping("/{userId}/card")
    public ResponseEntity<ResponseApi<PlayerCardDto>> getPlayerCard(@PathVariable UUID userId) {
        return ResponseEntity.ok(new ResponseApi<>("Player card fetched successfully", playerCardQueryService.getLiveCard(userId), true));
    }

    //istoricul cardului unui user(pt grafice)
    @GetMapping("/{userId}/card/history")
    public ResponseEntity<ResponseApi<List<PlayerCardHistoryPointDto>>> getPlayerCardHistory(@PathVariable UUID userId) {
        return ResponseEntity.ok(new ResponseApi<>("Player card history fetched successfully", playerCardQueryService.getCardHistory(userId), true));
    }

    @GetMapping("/{userId}/card/live-form")
    public ResponseEntity<ResponseApi<LiveFormDto>> getLiveForm(@PathVariable UUID userId) {
        return ResponseEntity.ok(new ResponseApi<>("Player live form fetched successfully", liveFormService.calculateLiveForm(userId), true));
    }

}
