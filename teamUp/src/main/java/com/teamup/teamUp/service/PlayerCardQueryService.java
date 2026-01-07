package com.teamup.teamUp.service;

import com.teamup.teamUp.exceptions.NotFoundException;
import com.teamup.teamUp.model.entity.PlayerCardStats;
import com.teamup.teamUp.model.entity.PlayerCardStatsHistory;
import com.teamup.teamUp.repository.PlayerCardStatsHistoryRepository;
import com.teamup.teamUp.repository.PlayerCardStatsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PlayerCardQueryService {

    private final PlayerCardStatsRepository cardStatsRepository;
    private final PlayerCardStatsHistoryRepository historyRepository;

    public PlayerCardQueryService(PlayerCardStatsRepository cardStatsRepository, PlayerCardStatsHistoryRepository historyRepository) {
        this.cardStatsRepository = cardStatsRepository;
        this.historyRepository = historyRepository;
    }

    //cardul live al userului
    public PlayerCardStats getLiveCard(UUID userId) {
        return cardStatsRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Player card not found"));
    }

    //istoric card pt grafice
    public List<PlayerCardStatsHistory> getCardHistory(UUID userId) {
        return historyRepository.findByUserIdOrderByRecordedAtAsc(userId);
    }
}
