package com.teamup.teamUp.controller;

import com.teamup.teamUp.model.dto.team.CreateTeamRequestDto;
import com.teamup.teamUp.model.dto.team.TeamMemberResponseDto;
import com.teamup.teamUp.model.dto.team.TeamResponseDto;
import com.teamup.teamUp.service.TeamService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/teams")
public class TeamController {
    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @PostMapping
    public ResponseEntity<ResponseApi<TeamResponseDto>> createTeam(@Valid @RequestBody CreateTeamRequestDto request, Authentication auth) {
        TeamResponseDto team = teamService.createTeam(request.getName(), auth.getName()); //capitanul este userul logat, cel care creaza echipa

        return ResponseEntity.ok(new ResponseApi<>("Team created successfully", team, true));
    }

    @PostMapping("/{teamId}/members/{userId}")
    public ResponseEntity<ResponseApi<Void>> addPlayer(@PathVariable UUID teamId, @PathVariable UUID userId, Authentication auth) {
        teamService.addPlayer(teamId, userId, auth.getName());

        return ResponseEntity.ok(new ResponseApi<>("Player added successfully", null, true));
    }

    @GetMapping("/{teamId}/members")
    public ResponseEntity<ResponseApi<List<TeamMemberResponseDto>>> getMembers(@PathVariable UUID teamId) {
        List<TeamMemberResponseDto> members = teamService.getMembers(teamId);
        return ResponseEntity.ok(new ResponseApi<>("Members retrieved successfully", members, true));
    }

    @GetMapping("/{teamId}")
    public ResponseEntity<ResponseApi<TeamResponseDto>> getTeam(@PathVariable UUID teamId) {
        TeamResponseDto team = teamService.getTeam(teamId);
        return ResponseEntity.ok(new ResponseApi<>("Team retrieved successfully", team, true));
    }

    @GetMapping("/my")
    public ResponseEntity<ResponseApi<List<TeamResponseDto>>> getMyTeams(Authentication auth) {
        return ResponseEntity.ok(new ResponseApi<>("My teams retrieved", teamService.getTeamsForUser(auth.getName()), true));
    }

    @DeleteMapping("/{teamId}/members/{userId}")
    public ResponseEntity<ResponseApi<Void>> removePlayer(@PathVariable UUID teamId, @PathVariable UUID userId, Authentication auth) {
        teamService.removePlayer(teamId, userId, auth.getName());
        return ResponseEntity.ok(new ResponseApi<>("Player removed successfully", null, true));
    }
}

