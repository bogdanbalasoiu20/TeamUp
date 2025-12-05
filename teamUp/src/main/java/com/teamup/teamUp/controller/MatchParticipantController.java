package com.teamup.teamUp.controller;


import com.teamup.teamUp.model.dto.matchParticipant.*;
import com.teamup.teamUp.model.entity.Match;
import com.teamup.teamUp.model.enums.MatchParticipantStatus;
import com.teamup.teamUp.service.MatchParticipantService;
import com.teamup.teamUp.service.MatchService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/matches")
public class MatchParticipantController {
    private final MatchParticipantService matchParticipantService;
    private final MatchService matchService;

    @Autowired
    public MatchParticipantController(MatchParticipantService matchParticipantService, MatchService matchService) {
        this.matchParticipantService = matchParticipantService;
        this.matchService = matchService;
    }

    @PostMapping("/{matchId}/participants/join")
    public ResponseEntity<ResponseApi<JoinWithStatusResponseDto>> join(@PathVariable UUID matchId, @RequestBody(required = false) @Valid JoinRequestDto request, Authentication auth) {
        var response = matchParticipantService.join(matchId,auth.getName(),request);
        return ResponseEntity.ok(new ResponseApi<>("User requested to join",response,true));
    }

    @DeleteMapping("/{matchId}/participants/leave")
    public ResponseEntity<ResponseApi<JoinResponseDto>> leave(@PathVariable UUID matchId, Authentication auth) {
        var response = matchParticipantService.leave(matchId,auth.getName());
        return ResponseEntity.ok(new ResponseApi<>("User left ",response,true));
    }

    @PostMapping("/{matchId}/participants/{userId}/approve")
    public ResponseEntity<ResponseApi<JoinResponseDto>> approve(
            @PathVariable UUID matchId,
            @PathVariable UUID userId,
            Authentication auth) {
        var response = matchParticipantService.approve(matchId, userId, auth.getName());
        return ResponseEntity.ok(new ResponseApi<>("Request approved", response, true));
    }


    @PostMapping("/{matchId}/participants/{userId}/reject")
    public ResponseEntity<ResponseApi<JoinResponseDto>> reject(
            @PathVariable UUID matchId,
            @PathVariable UUID userId,
            Authentication auth) {
        var resp = matchParticipantService.reject(matchId, userId, auth.getName());
        return ResponseEntity.ok(new ResponseApi<>("Request rejected", resp, true));
    }

    @PostMapping("/{matchId}/participants/{userId}/invite")
    public ResponseEntity<ResponseApi<JoinResponseDto>> invite(
            @PathVariable UUID matchId,
            @PathVariable UUID userId,
            Authentication auth) {
        var resp = matchParticipantService.invite(matchId, userId, auth.getName());
        return ResponseEntity.ok(new ResponseApi<>("User invited", resp, true));
    }

    @PostMapping("/{matchId}/participants/accept")
    public ResponseEntity<ResponseApi<JoinResponseDto>> accept(
            @PathVariable UUID matchId,
            Authentication auth) {
        var resp = matchParticipantService.acceptInvite(matchId, auth.getName());
        return ResponseEntity.ok(new ResponseApi<>("Invitation accepted", resp, true));
    }

    @PostMapping("/{matchId}/participants/decline")
    public ResponseEntity<ResponseApi<JoinResponseDto>> decline(
            @PathVariable UUID matchId,
            Authentication auth) {
        var resp = matchParticipantService.declineInvite(matchId, auth.getName());
        return ResponseEntity.ok(new ResponseApi<>("Invitation declined", resp, true));
    }

    @GetMapping("/{matchId}/participants")
    public ResponseEntity<ResponseApi<MatchParticipantsResponse>> list(
            @PathVariable UUID matchId,
            @RequestParam(required = false) MatchParticipantStatus status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        var page = matchParticipantService.listByStatus(matchId, status, pageable);

        Match match = matchService.findById(matchId);
        UUID creatorId = match.getCreator().getId();
        var resp = new MatchParticipantsResponse(creatorId, page);

        return ResponseEntity.ok(new ResponseApi<>("Participants fetched", resp, true));
    }


    @DeleteMapping("/{matchId}/participants/{userId}")
    public ResponseEntity<ResponseApi<JoinResponseDto>> kick(
            @PathVariable UUID matchId,
            @PathVariable UUID userId,
            Authentication auth) {
        var resp = matchParticipantService.kick(matchId, userId, auth.getName());
        return ResponseEntity.ok(new ResponseApi<>("Participant kicked", resp, true));
    }


    @PostMapping("/{matchId}/participants/{userId}/promote")
    public ResponseEntity<ResponseApi<ParticipantDto>> promoteFromWaitlist(
            @PathVariable UUID matchId,
            @PathVariable UUID userId,
            Authentication auth) {

        ParticipantDto dto = matchParticipantService.promoteFromWaitlist(
                matchId,
                userId,
                auth.getName()
        );

        return ResponseEntity.ok(
                new ResponseApi<>("User promoted from waitlist", dto, true)
        );
    }



}
