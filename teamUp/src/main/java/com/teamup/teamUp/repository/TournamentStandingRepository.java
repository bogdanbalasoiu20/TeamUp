package com.teamup.teamUp.repository;

import com.teamup.teamUp.model.entity.TournamentStanding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TournamentStandingRepository extends JpaRepository<TournamentStanding, UUID> {
    List<TournamentStanding> findByTournamentIdOrderByPointsDescGoalsForDesc(UUID tournamentId);
    Optional<TournamentStanding> findByTournamentIdAndTeamId(UUID tournamentId, UUID teamId);
    List<TournamentStanding> findByTeamId(UUID teamId);
    int countByTeamIdAndFinalPosition(UUID teamId, Integer finalPosition);
}
