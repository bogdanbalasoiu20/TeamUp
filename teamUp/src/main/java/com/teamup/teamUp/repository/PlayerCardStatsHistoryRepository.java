package com.teamup.teamUp.repository;

import com.teamup.teamUp.model.entity.PlayerCardStatsHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PlayerCardStatsHistoryRepository extends JpaRepository<PlayerCardStatsHistory, UUID> {
    List<PlayerCardStatsHistory> findByUserIdOrderByRecordedAtAsc(UUID userId);

    long countByUserId(UUID userId);

    List<PlayerCardStatsHistory>findTop3ByUserIdOrderByRecordedAtDesc(UUID userId);

}
