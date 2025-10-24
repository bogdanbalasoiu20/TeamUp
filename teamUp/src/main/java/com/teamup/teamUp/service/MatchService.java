package com.teamup.teamUp.service;

import com.teamup.teamUp.exceptions.NotFoundException;
import com.teamup.teamUp.model.entity.Match;
import com.teamup.teamUp.repository.MatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class MatchService {
    private final MatchRepository matchRepository;

    @Autowired
    public MatchService(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;
    }

    public Match findById(UUID id){
        return matchRepository.findById(id).orElseThrow(()-> new NotFoundException("Match not found"));
    }
}
