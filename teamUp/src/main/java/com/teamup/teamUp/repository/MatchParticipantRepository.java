package com.teamup.teamUp.repository;

import com.teamup.teamUp.model.entity.MatchParticipant;
import com.teamup.teamUp.model.id.MatchParticipantId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface MatchParticipantRepository extends JpaRepository<MatchParticipant, MatchParticipantId> {
    long countById_MatchId(UUID matchId);
    boolean existsById_MatchIdAndId_UserId(UUID matchId, UUID userId);

    @Query("select mp.user.id from MatchParticipant mp where mp.match.id = :matchId")
    List<UUID> findParticipantsIDs(UUID matchId);

    @Modifying
    @Query("delete from MatchParticipant mp where mp.match.id = :matchId and mp.user.id = :userId")
    int deleteByMatchIdAndUserId(UUID matchId, UUID userId);
}
