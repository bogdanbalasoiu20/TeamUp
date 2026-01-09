package com.teamup.teamUp.scheduler;

import com.teamup.teamUp.events.NotificationEvents;
import com.teamup.teamUp.model.entity.Match;
import com.teamup.teamUp.model.enums.MatchStatus;
import com.teamup.teamUp.repository.MatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MatchFinishPromptScheduler {

    private final MatchRepository matchRepository;
    private final NotificationEvents notificationEvents;

    @Scheduled(fixedDelay = 60 * 1000) // la 10 minute
    @Transactional
    public void notifyCreatorsToFinishMatch() {

        Instant now = Instant.now();

        List<Match> matches = matchRepository
                .findAllByStatusNotAndFinishPromptSentFalse(MatchStatus.DONE);

        for (Match match : matches) {

            if (match.getEndsAt() != null && now.isAfter(match.getEndsAt())) {

                notificationEvents.matchFinishConfirmationNeeded(match);

                match.setFinishPromptSent(true);
            }
        }
    }
}

