package com.teamup.teamUp.controller;


import com.teamup.teamUp.model.dto.matchParticipant.JoinRequestDto;
import com.teamup.teamUp.model.dto.matchParticipant.JoinResponseDto;
import com.teamup.teamUp.service.MatchParticipantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/matches")
public class MatchParticipantController {
    private final MatchParticipantService matchParticipantService;

    @Autowired
    public MatchParticipantController(MatchParticipantService matchParticipantService) {
        this.matchParticipantService = matchParticipantService;
    }

    @PostMapping("/{matchId}/join")
    public ResponseEntity<ResponseApi<JoinResponseDto>> join(@PathVariable UUID matchId, @RequestBody JoinRequestDto request, Authentication auth) {
        var response = matchParticipantService.join(matchId,auth.getName(),request);
        return ResponseEntity.ok(new ResponseApi<>("User joined",response,true));
    }
}
