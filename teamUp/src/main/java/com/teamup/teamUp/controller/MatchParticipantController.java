package com.teamup.teamUp.controller;


import com.teamup.teamUp.model.dto.matchParticipant.JoinRequestDto;
import com.teamup.teamUp.model.dto.matchParticipant.JoinResponseDto;
import com.teamup.teamUp.model.dto.matchParticipant.ParticipantDto;
import com.teamup.teamUp.model.enums.MatchParticipantStatus;
import com.teamup.teamUp.service.MatchParticipantService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

    @PostMapping("/{userId}/invite")
    public ResponseEntity<ResponseApi<JoinResponseDto>> invite(
            @PathVariable UUID matchId,
            @PathVariable UUID userId,
            Authentication auth) {
        var resp = matchParticipantService.invite(matchId, userId, auth.getName());
        return ResponseEntity.ok(new ResponseApi<>("User invited", resp, true));
    }

    @PostMapping("/accept")
    public ResponseEntity<ResponseApi<JoinResponseDto>> accept(
            @PathVariable UUID matchId,
            Authentication auth) {
        var resp = matchParticipantService.acceptInvite(matchId, auth.getName());
        return ResponseEntity.ok(new ResponseApi<>("Invitation accepted", resp, true));
    }

    @PostMapping("/decline")
    public ResponseEntity<ResponseApi<JoinResponseDto>> decline(
            @PathVariable UUID matchId,
            Authentication auth) {
        var resp = matchParticipantService.declineInvite(matchId, auth.getName());
        return ResponseEntity.ok(new ResponseApi<>("Invitation declined", resp, true));
    }

    @GetMapping
    public ResponseEntity<ResponseApi<Page<ParticipantDto>>> list(
            @PathVariable UUID matchId,
            @RequestParam(required = false) MatchParticipantStatus status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        var page = matchParticipantService.listByStatus(matchId, status, pageable);
        return ResponseEntity.ok(new ResponseApi<>("Participants fetched", page, true));
    }


    @DeleteMapping("/{userId}")
    public ResponseEntity<ResponseApi<JoinResponseDto>> kick(
            @PathVariable UUID matchId,
            @PathVariable UUID userId,
            Authentication auth) {
        var resp = matchParticipantService.kick(matchId, userId, auth.getName());
        return ResponseEntity.ok(new ResponseApi<>("Participant kicked", resp, true));
    }


}
