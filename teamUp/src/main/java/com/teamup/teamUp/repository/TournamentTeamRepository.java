package com.teamup.teamUp.repository;

import com.teamup.teamUp.model.entity.TournamentTeam;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TournamentTeamRepository extends JpaRepository<TournamentTeam, UUID> {
    boolean existsByTournamentIdAndTeamId(UUID tournamentId, UUID teamId);
    List<TournamentTeam> findByTournamentId(UUID tournamentId);
}
