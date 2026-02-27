package com.teamup.teamUp.repository;

import com.teamup.teamUp.model.entity.TeamChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.UUID;

public interface TeamChatRepository extends JpaRepository<TeamChatMessage, UUID> {

    @Query("""
            SELECT m FROM TeamChatMessage m
            WHERE m.team.id = :teamId
            order by m.createdAt asc
""")
    Page<TeamChatMessage> findInitialMessages(UUID teamId, Pageable pageable);

    Page<TeamChatMessage> findByTeamIdAndCreatedAtAfter(UUID teamId, Instant after,Pageable pageable);
}
