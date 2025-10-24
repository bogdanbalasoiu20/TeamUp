package com.teamup.teamUp.controller;

import com.teamup.teamUp.mapper.MatchMapper;
import com.teamup.teamUp.model.dto.match.MatchResponseDto;
import com.teamup.teamUp.model.entity.Match;
import com.teamup.teamUp.service.MatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
