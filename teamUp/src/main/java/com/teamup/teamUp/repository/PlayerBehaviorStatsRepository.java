package com.teamup.teamUp.repository;

import com.teamup.teamUp.model.entity.PlayerBehaviorStats;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PlayerBehaviorStatsRepository extends JpaRepository<PlayerBehaviorStats, UUID> {
    Optional<PlayerBehaviorStats> findByUser_Id(UUID userId);
}
