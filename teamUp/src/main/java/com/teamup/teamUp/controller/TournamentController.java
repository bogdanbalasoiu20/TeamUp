package com.teamup.teamUp.controller;

import com.teamup.teamUp.model.dto.tournament.CreateTournamentRequestDto;
import com.teamup.teamUp.model.dto.tournament.TournamentMatchResponseDto;
import com.teamup.teamUp.model.dto.tournament.TournamentResponseDto;
import com.teamup.teamUp.model.dto.tournament.TournamentStandingResponseDto;
import com.teamup.teamUp.service.TournamentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tournaments")
public class TournamentController {

    private final TournamentService tournamentService;

    public TournamentController(TournamentService tournamentService) {
        this.tournamentService = tournamentService;
    }

    @PostMapping
    public ResponseEntity<ResponseApi<TournamentResponseDto>> createTournament(@Valid @RequestBody CreateTournamentRequestDto request, Authentication auth) {
        TournamentResponseDto tournament = tournamentService.createTournament(request, auth.getName());
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

    @GetMapping("/{tournamentId}")
    public ResponseEntity<ResponseApi<TournamentResponseDto>> getTournament(@PathVariable UUID tournamentId) {
        TournamentResponseDto tournament = tournamentService.getTournament(tournamentId);
        return ResponseEntity.ok(new ResponseApi<>("Tournament retrieved successfully", tournament, true));
    }

    @GetMapping("/{tournamentId}/matches")
    public ResponseEntity<ResponseApi<List<TournamentMatchResponseDto>>> getMatches(@PathVariable UUID tournamentId) {
        List<TournamentMatchResponseDto> matches = tournamentService.getMatches(tournamentId);
        return ResponseEntity.ok(new ResponseApi<>("Matches retrieved successfully", matches, true));
    }

    @GetMapping("/{tournamentId}/standings")
    public ResponseEntity<ResponseApi<List<TournamentStandingResponseDto>>> getStandings(@PathVariable UUID tournamentId) {
        List<TournamentStandingResponseDto> standings = tournamentService.getStandings(tournamentId);
        return ResponseEntity.ok(new ResponseApi<>("Standings retrieved successfully", standings, true));
    }

    @GetMapping
    public ResponseEntity<ResponseApi<List<TournamentResponseDto>>> getAllTournaments() {
        List<TournamentResponseDto> tournaments = tournamentService.getAllTournaments();
        return ResponseEntity.ok(new ResponseApi<>("Tournaments retrieved successfully", tournaments, true));
    }

}

