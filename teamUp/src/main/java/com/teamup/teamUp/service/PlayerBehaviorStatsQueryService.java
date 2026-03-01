package com.teamup.teamUp.service;

import com.teamup.teamUp.model.dto.rating.player.PlayerBehaviorStatsDto;
import com.teamup.teamUp.model.entity.PlayerBehaviorStats;
import com.teamup.teamUp.repository.PlayerBehaviorStatsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PlayerBehaviorStatsQueryService {
    private final PlayerBehaviorStatsRepository statsRepo;

    public PlayerBehaviorStatsQueryService(PlayerBehaviorStatsRepository statsRepo) {
        this.statsRepo = statsRepo;
    }

    public PlayerBehaviorStatsDto getBehaviorStats(UUID userId) {
        PlayerBehaviorStats stats = statsRepo.findByUser_Id(userId).orElseThrow(() -> new IllegalStateException("Behavior stats not found"));

        return new PlayerBehaviorStatsDto(
                (int) Math.round(stats.getFairPlay()),
                (int) Math.round(stats.getCommunication()),
                (int) Math.round(stats.getFun()),
                (int) Math.round(stats.getCompetitiveness()),
                (int) Math.round(stats.getSelfishness()),
                (int) Math.round(stats.getAggressiveness()),
                stats.getFeedbackCount()
        );
    }
}

