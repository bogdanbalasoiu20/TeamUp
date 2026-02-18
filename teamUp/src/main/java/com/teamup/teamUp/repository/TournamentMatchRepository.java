package com.teamup.teamUp.repository;

import com.teamup.teamUp.model.entity.TournamentMatch;
import com.teamup.teamUp.model.enums.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TournamentMatchRepository extends JpaRepository<TournamentMatch, UUID> {
    List<TournamentMatch> findByTournamentId(UUID tournamentId);
    List<TournamentMatch> findByTournamentIdAndStatus(UUID tournamentId, MatchStatus status);
}
