package com.teamup.teamUp.scheduler;

import com.teamup.teamUp.model.entity.Match;
import com.teamup.teamUp.repository.MatchRepository;
import com.teamup.teamUp.service.RatingUpdateService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class RatingFinalizationScheduler {

    private static final Duration RATING_WINDOW = Duration.ofHours(24);

    private final MatchRepository matchRepository;
    private final RatingUpdateService ratingUpdateService;

    public RatingFinalizationScheduler(
            MatchRepository matchRepository,
            RatingUpdateService ratingUpdateService
    ) {
        this.matchRepository = matchRepository;
        this.ratingUpdateService = ratingUpdateService;
    }

    @Scheduled(fixedDelay = 5 * 60 * 1000)
    @Transactional
    public void finalizeExpiredRatings() {

        Instant deadline = Instant.now().minus(RATING_WINDOW);

        List<Match> matches = matchRepository.findMatchesToFinalize(deadline);

        for (Match match : matches) {

            ratingUpdateService.updateAfterMatch(match.getId());

            match.setRatingsFinalized(true);
            matchRepository.save(match);
        }
    }
}

