package com.teamup.teamUp.controller;

import com.teamup.teamUp.model.dto.tournament.FinishMatchRequestDto;
import com.teamup.teamUp.service.TournamentMatchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/tournament-matches")
public class TournamentMatchController {

    private final TournamentMatchService matchService;

    public TournamentMatchController(TournamentMatchService matchService) {
        this.matchService = matchService;
    }

    @PostMapping("/{matchId}/finish")
    public ResponseEntity<ResponseApi<Void>> finishMatch(
            @PathVariable UUID matchId,
            @RequestBody FinishMatchRequestDto request
    ) {

        matchService.finishMatch(
                matchId,
                request.getScoreHome(),
                request.getScoreAway()
        );

        return ResponseEntity.ok(
                new ResponseApi<>("Match finished successfully", null, true)
        );
    }
}
