package com.teamup.teamUp.repository;

import com.teamup.teamUp.model.entity.MatchParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface MatchParticipantRepository extends JpaRepository<MatchParticipant, UUID> {
    long countByMatchId(UUID matchId);
    boolean existsByMatchIdAndUserId(UUID matchId, UUID userId);

    @Query("select mp.user.id from MatchParticipant mp where mp.match.id = :matchId")
    List<UUID> findParticipantsIDs(UUID matchId);

    int deleteByMatchIdAndUserId(UUID matchId, UUID userId);
}
