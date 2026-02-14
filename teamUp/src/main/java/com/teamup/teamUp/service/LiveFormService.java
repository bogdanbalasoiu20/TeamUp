package com.teamup.teamUp.service;

import com.teamup.teamUp.model.dto.liveform.LiveFormDto;
import com.teamup.teamUp.model.entity.PlayerCardStats;
import com.teamup.teamUp.model.entity.PlayerCardStatsHistory;
import com.teamup.teamUp.repository.PlayerCardStatsHistoryRepository;
import com.teamup.teamUp.repository.PlayerCardStatsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class LiveFormService {

    private static final double MAX_FORM_DELTA = 2.0;

    private final PlayerCardStatsRepository cardRepo;
    private final PlayerCardStatsHistoryRepository historyRepo;

    public LiveFormService(PlayerCardStatsRepository cardRepo, PlayerCardStatsHistoryRepository historyRepo) {
        this.cardRepo = cardRepo;
        this.historyRepo = historyRepo;
    }

    @Transactional(readOnly = true)
    public LiveFormDto calculateLiveForm(UUID userId) {

        PlayerCardStats card = cardRepo.findById(userId).orElseThrow(() -> new IllegalStateException("Player card not found"));

        List<PlayerCardStatsHistory> lastMatches = historyRepo.findTop3ByUserIdAndMatchIdIsNotNullOrderByRecordedAtDesc(userId);

        int matchesCount = lastMatches.size();

        if (matchesCount == 0) {
            return new LiveFormDto(
                    round(card.getOverallRating()),
                    0.0,
                    0
            );
        }

        if (matchesCount == 1) {
            double last = lastMatches.get(0).getOverallRating();
            return new LiveFormDto(
                    round(last),
                    0.0,
                    1
            );
        }

        double lastOverall = lastMatches.get(0).getOverallRating();

        double previousAvg = lastMatches.subList(1, matchesCount)
                .stream()
                .mapToDouble(PlayerCardStatsHistory::getOverallRating)
                .average()
                .orElse(lastOverall);

        double rawDelta = lastOverall - previousAvg;
        double clampedDelta = clamp(rawDelta, -MAX_FORM_DELTA, MAX_FORM_DELTA);

        return new LiveFormDto(
                round(lastOverall),
                round(clampedDelta),
                matchesCount
        );
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
