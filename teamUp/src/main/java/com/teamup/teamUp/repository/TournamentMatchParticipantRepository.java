package com.teamup.teamUp.repository;

import com.teamup.teamUp.model.entity.TournamentMatchParticipant;
import com.teamup.teamUp.model.id.TournamentMatchParticipantId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TournamentMatchParticipantRepository extends JpaRepository<TournamentMatchParticipant, TournamentMatchParticipantId> {
    boolean existsByMatch_Id(UUID matchId);
}
