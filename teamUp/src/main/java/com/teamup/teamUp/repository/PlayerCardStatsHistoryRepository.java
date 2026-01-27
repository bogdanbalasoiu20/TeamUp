package com.teamup.teamUp.repository;

import com.teamup.teamUp.model.entity.PlayerCardStatsHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PlayerCardStatsHistoryRepository extends JpaRepository<PlayerCardStatsHistory, UUID> {
    List<PlayerCardStatsHistory> findByUserIdOrderByRecordedAtAsc(UUID userId);

    long countByUserId(UUID userId);

    List<PlayerCardStatsHistory>findTop3ByUserIdOrderByRecordedAtDesc(UUID userId);

    @Query("""
        SELECT COALESCE(MAX(h.overallRating), 0)
        FROM PlayerCardStatsHistory h
        WHERE h.userId = :userId
    """)
    double findMaxOverallByUserId(@Param("userId") UUID userId);

}
