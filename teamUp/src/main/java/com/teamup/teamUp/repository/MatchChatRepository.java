package com.teamup.teamUp.repository;

import com.teamup.teamUp.model.entity.MatchChatMessage;
import com.teamup.teamUp.model.enums.MatchParticipantStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

public interface MatchChatRepository extends JpaRepository<MatchChatMessage, UUID> {

    @EntityGraph(attributePaths = "sender")
    @Query("""
        select m from MatchChatMessage m
        where m.match.id = :matchId
        order by m.createdAt asc, m.id asc
    """)
    Page<MatchChatMessage> findInitialMessages(
            @Param("matchId") UUID matchId,
            Pageable pageable
    );

    @EntityGraph(attributePaths = "sender")
    Page<MatchChatMessage> findByMatchIdAndCreatedAtAfter(
            UUID matchId,
            Instant createdAt,
            Pageable pageable
    );
}

