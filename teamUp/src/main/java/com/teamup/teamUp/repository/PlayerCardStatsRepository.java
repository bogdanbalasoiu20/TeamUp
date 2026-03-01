package com.teamup.teamUp.repository;

import com.teamup.teamUp.model.entity.PlayerCardStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PlayerCardStatsRepository extends JpaRepository<PlayerCardStats, UUID> {
    @Query("""
        SELECT p.overallRating
        FROM PlayerCardStats p
        WHERE p.userId = :userId
    """)
    Double getOverall(@Param("userId") UUID userId);

    @Query("""
    SELECT p.userId, p.overallRating
    FROM PlayerCardStats p
    WHERE p.userId IN :userIds
""")
    List<Object[]> findOverallForUsers(@Param("userIds") List<UUID> userIds);
}
