package com.teamup.teamUp.controller;


import com.teamup.teamUp.model.dto.matchParticipant.JoinRequestDto;
import com.teamup.teamUp.model.dto.matchParticipant.JoinResponseDto;
import com.teamup.teamUp.service.MatchParticipantService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/matches/{matchId}/participants")
public class MatchParticipantController {
    private final MatchParticipantService matchParticipantService;

    @Autowired
    public MatchParticipantController(MatchParticipantService matchParticipantService) {
        this.matchParticipantService = matchParticipantService;
    }

    @PostMapping("/join")
    public ResponseEntity<ResponseApi<JoinResponseDto>> join(@PathVariable UUID matchId, @RequestBody(required = false) @Valid JoinRequestDto request, Authentication auth) {
        var response = matchParticipantService.join(matchId,auth.getName(),request);
        return ResponseEntity.ok(new ResponseApi<>("User joined",response,true));
    }

    @DeleteMapping("/leave")
    public ResponseEntity<ResponseApi<JoinResponseDto>> leave(@PathVariable UUID matchId, Authentication auth) {
        var response = matchParticipantService.leave(matchId,auth.getName());
        return ResponseEntity.ok(new ResponseApi<>("User left ",response,true));
    }

    @PostMapping("/{userId}/approve")
    public ResponseEntity<ResponseApi<JoinResponseDto>> approve(
            @PathVariable UUID matchId,
            @PathVariable UUID userId,
            Authentication auth) {
        var response = matchParticipantService.approve(matchId, userId, auth.getName());
        return ResponseEntity.ok(new ResponseApi<>("Request approved", response, true));
    }


    @PostMapping("/{userId}/reject")
    public ResponseEntity<ResponseApi<JoinResponseDto>> reject(
            @PathVariable UUID matchId,
            @PathVariable UUID userId,
            Authentication auth) {
        var resp = matchParticipantService.reject(matchId, userId, auth.getName());
        return ResponseEntity.ok(new ResponseApi<>("Request rejected", resp, true));
    }

}
