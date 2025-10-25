package com.teamup.teamUp.service;

import com.teamup.teamUp.exceptions.BadRequestException;
import com.teamup.teamUp.exceptions.NotFoundException;
import com.teamup.teamUp.model.dto.match.MatchCreateRequestDto;
import com.teamup.teamUp.model.entity.Match;
import com.teamup.teamUp.model.entity.User;
import com.teamup.teamUp.model.entity.Venue;
import com.teamup.teamUp.model.enums.MatchVisibility;
import com.teamup.teamUp.repository.MatchRepository;
import com.teamup.teamUp.repository.UserRepository;
import com.teamup.teamUp.repository.VenueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class MatchService {
    private final MatchRepository matchRepository;
    private final UserRepository userRepository;
    private final VenueRepository venueRepository;

    @Autowired
    public MatchService(MatchRepository matchRepository,  UserRepository userRepository,  VenueRepository venueRepository) {
        this.matchRepository = matchRepository;
        this.userRepository = userRepository;
        this.venueRepository = venueRepository;
    }

    public Match findById(UUID id){
        return matchRepository.findById(id).orElseThrow(()-> new NotFoundException("Match not found"));
    }

    @Transactional
    public Match create(MatchCreateRequestDto request, String authUsername){
        User user = userRepository.findByUsernameIgnoreCaseAndDeletedFalse(authUsername).orElseThrow(() -> new NotFoundException("User not found"));
        Venue venue = venueRepository.findById(request.venueId()).orElseThrow(() -> new NotFoundException("Venue not found"));
        if(Boolean.FALSE.equals(venue.getIsActive())){
            throw new BadRequestException("Venue not active");
        }

        MatchVisibility matchVisibility = (request.visibility() == null) ? MatchVisibility.PUBLIC : request.visibility();

        var match = Match.builder()
                .creator(user)
                .venue(venue)
                .startsAt(request.startsAt())
                .durationMinutes(request.durationMinutes())
                .maxPlayers(request.maxPlayers())
                .title(request.title().trim())
                .notes(request.notes().trim())
                .visibility(matchVisibility)
                .joinDeadline(request.joinDeadline())
                .totalPrice(request.totalPrice())
                .build();

        return matchRepository.save(match);
    }
}
