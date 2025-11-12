package com.teamup.teamUp.controller;

import com.teamup.teamUp.model.dto.rating.PlayerRatingDto;
import com.teamup.teamUp.model.dto.rating.PlayerToRateDto;
import com.teamup.teamUp.service.PlayerStatsService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/matches")
public class PlayerRatingController {
    private final PlayerStatsService playerStatsService;

    @Autowired
    public PlayerRatingController(PlayerStatsService playerStatsService) {
        this.playerStatsService = playerStatsService;
    }

    //lista jucatorilor din meci pentru a fi notati de userul curent
    @GetMapping("/{matchId}/ratings")
    public ResponseEntity<ResponseApi<List<PlayerToRateDto>>> getPlayersToRate(@PathVariable UUID matchId, Authentication auth){
        var response = playerStatsService.getPlayersToRate(matchId, auth.getName());
        return ResponseEntity.ok(new ResponseApi<>("Players to rate fetched successfully",response,true));
    }

    //jucatorul curent trimite rantigurile acordate colegilor
    @PostMapping("/{matchId}/ratings")
    public ResponseEntity<ResponseApi<Void>> submitRatings(@Valid @RequestBody List<PlayerRatingDto> ratings, @PathVariable UUID matchId, Authentication auth){
        playerStatsService.submitRatings(matchId,auth.getName(),ratings);
        return ResponseEntity.ok(new ResponseApi<>("Ratings submitted successfully", null, true));
    }
}
