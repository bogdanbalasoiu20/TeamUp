package com.teamup.teamUp.controller;

import com.teamup.teamUp.model.dto.team.CreateTeamRequestDto;
import com.teamup.teamUp.model.entity.Team;
import com.teamup.teamUp.service.TeamService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/teams")
public class TeamController {
    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @PostMapping
    public ResponseEntity<ResponseApi<Team>> createTeam(@RequestBody CreateTeamRequestDto request, Authentication auth) {
        Team team = teamService.createTeam(request.getName(), auth.getName()); //capitanul este userul logat, cel care creaza echipa

        return ResponseEntity.ok(new ResponseApi<>("Team created successfully", team, true));
    }

    @PostMapping("/{teamId}/members/{userId}")
    public ResponseEntity<ResponseApi<Void>> addPlayer(@PathVariable UUID teamId, @PathVariable UUID userId, Authentication auth) {
        teamService.addPlayer(teamId, userId, auth.getName());

        return ResponseEntity.ok(new ResponseApi<>("Player added successfully", null, true));
    }
}

