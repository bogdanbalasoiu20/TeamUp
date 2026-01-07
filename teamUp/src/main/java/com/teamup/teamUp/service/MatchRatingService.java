package com.teamup.teamUp.service;

import com.teamup.teamUp.exceptions.BadRequestException;
import com.teamup.teamUp.exceptions.NotFoundException;
import com.teamup.teamUp.model.entity.Match;
import com.teamup.teamUp.model.enums.MatchStatus;
import com.teamup.teamUp.repository.MatchRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class MatchRatingService {

    private final MatchRepository matchRepository;
    private final RatingUpdateService ratingUpdateService;

    public MatchRatingService(
            MatchRepository matchRepository,
            RatingUpdateService ratingUpdateService
    ) {
        this.matchRepository = matchRepository;
        this.ratingUpdateService = ratingUpdateService;
    }

    public void finalizeRatings(UUID matchId, String authUsername) {

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new NotFoundException("Match not found"));

        if (match.getStatus() != MatchStatus.DONE) {
            throw new BadRequestException("Match is not finished yet");
        }

        if (Boolean.TRUE.equals(match.getRatingsFinalized())) {
            throw new BadRequestException("Ratings already finalized");
        }

        ratingUpdateService.updateAfterMatch(matchId);

        match.setRatingsFinalized(true);
        matchRepository.save(match);
    }
}

