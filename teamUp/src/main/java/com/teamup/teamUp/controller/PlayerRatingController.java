package com.teamup.teamUp.controller;

import com.teamup.teamUp.model.dto.rating.player.PlayerRatingDto;
import com.teamup.teamUp.model.dto.rating.player.PlayerToRateDto;
import com.teamup.teamUp.service.PlayerRatingQueryService;
import com.teamup.teamUp.service.PlayerRatingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/matches")
public class PlayerRatingController {

    private final PlayerRatingService playerRatingService;
    private final PlayerRatingQueryService playerRatingQueryService;

    public PlayerRatingController(PlayerRatingService playerRatingService, PlayerRatingQueryService playerRatingQueryService) {
        this.playerRatingService = playerRatingService;
        this.playerRatingQueryService = playerRatingQueryService;
    }

    //lista jucatorilor din meci care pot fi evaluati
    @GetMapping("/{matchId}/ratings")
    public ResponseEntity<ResponseApi<List<PlayerToRateDto>>> getPlayersToRate(@PathVariable UUID matchId, Authentication auth) {
        var response = playerRatingQueryService.getPlayersToRate(matchId, auth.getName());
        return ResponseEntity.ok(new ResponseApi<>("Players to rate fetched successfully", response, true));
    }

    //submit pentru ratingurile colegilor din meci
    @PostMapping("/{matchId}/ratings")
    public ResponseEntity<ResponseApi<Void>> submitRatings(@PathVariable UUID matchId, @Valid @RequestBody List<PlayerRatingDto> ratings, Authentication auth) {
        playerRatingService.submitRatings(matchId, auth.getName(), ratings);
        return ResponseEntity.ok(new ResponseApi<>("Ratings submitted successfully", null, true));
    }
}
