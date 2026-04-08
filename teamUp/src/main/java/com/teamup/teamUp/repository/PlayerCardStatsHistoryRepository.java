package com.teamup.teamUp.repository;

import com.teamup.teamUp.model.entity.PlayerCardStatsHistory;
import com.teamup.teamUp.model.enums.EventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PlayerCardStatsHistoryRepository extends JpaRepository<PlayerCardStatsHistory, UUID> {
    List<PlayerCardStatsHistory> findByUserIdOrderByRecordedAtAsc(UUID userId);

    long countByUserIdAndEventType(UUID userId, EventType eventType);

    List<PlayerCardStatsHistory> findTop3ByUserIdAndEventTypeOrderByRecordedAtDesc(UUID userId, EventType eventType);

    @Query("""
        SELECT COALESCE(MAX(h.overallRating), 0)
        FROM PlayerCardStatsHistory h
        WHERE h.userId = :userId
    """)
    double findMaxOverallByUserId(@Param("userId") UUID userId);


    @Query("""
        select p
        from PlayerCardStatsHistory p
        where p.userId = :userId
        order by p.recordedAt desc
    """)
    Page<PlayerCardStatsHistory> findLatestStats(UUID userId, Pageable pageable);

    boolean existsByUserIdAndContextId(UUID userId, UUID contextId);

}
