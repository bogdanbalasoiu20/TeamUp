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
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MatchFinishPromptScheduler {

    private final MatchRepository matchRepository;
    private final NotificationEvents notificationEvents;

    @Scheduled(fixedDelay = 60 * 1000) // la 1 minut
    @Transactional
    public void notifyCreatorsToFinishMatch() {

        Instant now = Instant.now();

        List<Match> matches = matchRepository
                .findAllByStatusNotAndFinishPromptSentFalse(MatchStatus.DONE);

        for (Match match : matches) {

            Instant matchEnd = match.getStartsAt()
                    .plus(match.getDurationMinutes(), ChronoUnit.MINUTES);

            if (now.isAfter(matchEnd)) {
                notificationEvents.matchFinishConfirmationNeeded(match);
                match.setFinishPromptSent(true);
            }

        }
    }
}

