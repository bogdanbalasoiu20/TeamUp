package com.teamup.teamUp.controller;

import com.teamup.teamUp.mapper.MatchMapper;
import com.teamup.teamUp.model.dto.match.MatchCreateRequestDto;
import com.teamup.teamUp.model.dto.match.MatchResponseDto;
import com.teamup.teamUp.model.entity.Match;
import com.teamup.teamUp.service.MatchService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("api/matches")
public class MatchController {
    private final MatchService matchService;
    private final MatchMapper matchMapper;

    @Autowired
    public MatchController(MatchService matchService, MatchMapper matchMapper) {
        this.matchService = matchService;
        this.matchMapper = matchMapper;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseApi<MatchResponseDto>> getById(@PathVariable UUID id){
        Match match = matchService.findById(id);
        return ResponseEntity.ok(new ResponseApi<>("Match found successfully",matchMapper.toDto(match),true));
    }

    @PostMapping
    public ResponseEntity<ResponseApi<MatchResponseDto>> create(@Valid @RequestBody MatchCreateRequestDto request, Authentication auth){
        Match match = matchService.create(request,auth.getName());
        return ResponseEntity.created(URI.create("/api/matches/" + match.getId())).body(new ResponseApi<>("Match created successfully",matchMapper.toDto(match),true));
    }
}
