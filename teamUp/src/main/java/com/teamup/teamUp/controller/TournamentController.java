package com.teamup.teamUp.controller;

import com.teamup.teamUp.model.dto.tournament.CreateTournamentRequestDto;
import com.teamup.teamUp.model.entity.Tournament;
import com.teamup.teamUp.service.TournamentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/tournaments")
public class TournamentController {

    private final TournamentService tournamentService;

    public TournamentController(TournamentService tournamentService) {
        this.tournamentService = tournamentService;
    }

    @PostMapping
    public ResponseEntity<ResponseApi<Tournament>> createTournament(@RequestBody CreateTournamentRequestDto request, Authentication auth) {
        Tournament tournament = tournamentService.createTournament(request, auth.getName());
        return ResponseEntity.ok(new ResponseApi<>("Tournament created successfully", tournament, true));
    }


    @PostMapping("/{tournamentId}/join/{teamId}")
    public ResponseEntity<ResponseApi<Void>> joinTournament(@PathVariable UUID tournamentId, @PathVariable UUID teamId) {
        tournamentService.joinTournament(tournamentId, teamId);
        return ResponseEntity.ok(new ResponseApi<>("Team joined successfully", null, true));
    }

    @PostMapping("/{tournamentId}/start")
    public ResponseEntity<ResponseApi<Void>> startTournament(@PathVariable UUID tournamentId, Authentication auth) {
        tournamentService.startTournament(tournamentId,  auth.getName());
        return ResponseEntity.ok(new ResponseApi<>("Tournament started successfully", null, true));
    }
}

